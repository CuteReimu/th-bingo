package org.tfcc.bingo

import org.apache.logging.log4j.kotlin.logger
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.ss.usermodel.CellType.STRING
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object SpellConfig {
    /** 标准赛和Link赛用同一个配置 */
    const val NORMAL_GAME = 1

    /** BP赛的配置 */
    const val BP_GAME = 2

    private val cache = mapOf(
        NORMAL_GAME to Config(::buildNormalSpell),
        BP_GAME to Config(::buildBPSpell),
    ) // 因为用的SingleThreadExecutor，因此无需考虑线程安全问题

    /**
     * 随符卡
     * @param type 可以传入 [NORMAL_GAME] 或 [BP_GAME]
     * @param exPos ex符卡的位置
     * @param stars 星级的分布
     *
     * map：星级 int ->是否EX boolean->作品 String->Spell
     */
    fun get(
        type: Int,
        games: Array<String>,
        ranks: Array<String>?,
        exPos: IntArray,
        stars: IntArray,
        rand: Random
    ): Array<Spell> {
        val map = HashMap<Int, HashMap<Boolean, HashMap<String, LinkedList<Spell>>>>()
        for ((star, isExMap) in get(type)) {
            val isExMap2 = HashMap<Boolean, HashMap<String, LinkedList<Spell>>>()
            for ((isEx, gameMap) in isExMap) {
                for ((game, spellList) in gameMap) {
                    if (game !in games) continue
                    val spellList2 = spellList.filter { ranks == null || it.rank in ranks }
                    if (spellList2.isNotEmpty()) {
                        val gameMap2 = isExMap2.getOrPut(isEx) { HashMap<String, LinkedList<Spell>>() }
                        gameMap2[game] = LinkedList(spellList2.shuffled(rand))
                    }
                }
            }
            if (isExMap2.isNotEmpty()) map[star] = isExMap2
        }
        val spellIds = HashSet<String>()
        val result = Array(stars.size) {
            val isExMap = map[stars[it]] ?: throw HandlerException("${stars[it]}星符卡数量不足")
            val gameMap = isExMap[false] ?: throw HandlerException("${stars[it]}星符卡数量不足")
            var spell: Spell
            do {
                val game = gameMap.keys.randomOrNull(rand) ?: throw HandlerException("${stars[it]}星符卡数量不足")
                val spellList = gameMap[game]!!
                spell = spellList.removeFirst()
                if (spellList.isEmpty()) gameMap.remove(game)
            } while (!spellIds.add("${spell.game}-${spell.id}"))
            spell
        }
        for (i in exPos.indices) {
            var index = exPos[i]
            var firstTry = true
            tryOnce@ while (true) {
                if (firstTry) {
                    firstTry = false
                } else {
                    index = (index + 1) % result.size
                    if (index == exPos[i]) throw HandlerException("EX符卡数量不足")
                    if (index in exPos) continue
                }
                val isExMap = map[stars[index]] ?: continue
                val gameMap = isExMap[true] ?: continue
                var spell: Spell
                do {
                    val game = gameMap.keys.randomOrNull(rand) ?: continue@tryOnce
                    val spellList = gameMap[game]!!
                    spell = spellList.removeFirst()
                    if (spellList.isEmpty()) gameMap.remove(game)
                } while (!spellIds.add("${spell.game}-${spell.id}"))
                exPos[i] = index
                result[index] = spell
                break
            }
        }
        return result
    }

    /**
     * 随符卡
     * @param type 可以传入 [NORMAL_GAME]
     * @param exPos ex符卡的位置
     * @param stars 星级的分布
     */
    fun getOD(
        type: Int,
        games: Array<String>,
        ranks: Array<String>?,
        exPos: IntArray,
        stars: IntArray,
        rand: Random
    ): Array<Spell> {
        val map = HashMap<Int, HashMap<Boolean, HashMap<String, LinkedList<Spell>>>>()
        for ((star, isExMap) in get(type)) {
            val isExMap2 = HashMap<Boolean, HashMap<String, LinkedList<Spell>>>()
            for ((isEx, gameMap) in isExMap) {
                for ((game, spellList) in gameMap) {
                    if (game !in games) continue
                    val spellList2 = spellList.filter { ranks == null || it.rank in ranks }
                    if (spellList2.isNotEmpty()) {
                        val gameMap2 = isExMap2.getOrPut(isEx) { HashMap<String, LinkedList<Spell>>() }
                        gameMap2[game] = LinkedList(spellList2.shuffled(rand))
                    }
                }
            }
            if (isExMap2.isNotEmpty()) map[star] = isExMap2
        }
        val spellIds = HashSet<String>()
        val result = arrayOfNulls<Spell>(stars.size)

        // 原有的4/5级卡抽取逻辑保留，保证基础的分布
        for (i in stars.indices) {
            val requireStar = stars[i]
            if (requireStar !in 4..5) continue

            val isExMap = map[requireStar] ?: throw HandlerException("${requireStar}星符卡不足")
            val gameMap = isExMap[false] ?: throw HandlerException("${requireStar}星符卡不足")

            var spell: Spell
            do {
                val game = gameMap.keys.randomOrNull(rand) ?: throw HandlerException("${requireStar}星符卡不足")
                val spellList = gameMap[game]!!
                spell = spellList.removeFirst()
                if (spellList.isEmpty()) gameMap.remove(game)
            } while (!spellIds.add("${spell.game}-${spell.id}"))

            result[i] = spell
        }

        // 5级卡替换，若5级卡数量不足，将其替换为待替换的4级。
        val star15Indices = stars.indices.filter { stars[it] == 15 }.toMutableList()
        star15Indices.shuffle(rand) // 替换项洗牌，保证卡不足时分布均匀
        for (i in star15Indices) {
            try {
                val isExMap = map[5] ?: continue
                val gameMap = isExMap[false] ?: continue

                var spell: Spell? = null
                retry@ while (true) {
                    val game = gameMap.keys.randomOrNull(rand) ?: break@retry
                    val spellList = gameMap[game] ?: continue
                    if (spellList.isEmpty()) {
                        gameMap.remove(game)
                        continue
                    }
                    spell = spellList.removeFirst()
                    if (spellList.isEmpty()) gameMap.remove(game)
                    if (spellIds.add("${spell.game}-${spell.id}")) break@retry
                }

                spell?.let {
                    result[i] = it
                } ?: run {
                    stars[i] = 14 // 降级处理
                }
            } catch (e: Exception) {
                stars[i] = 14
            }
        }

        // 4级卡替换。不足则改为生成3级。
        val star14Indices = stars.indices.filter { stars[it] == 14 }.toMutableList()
        star14Indices.shuffle(rand)
        for (i in star14Indices) {
            try {
                val isExMap = map[4] ?: continue
                val gameMap = isExMap[false] ?: continue

                var spell: Spell? = null
                retry@ while (true) {
                    val game = gameMap.keys.randomOrNull(rand) ?: break@retry
                    val spellList = gameMap[game] ?: continue
                    if (spellList.isEmpty()) {
                        gameMap.remove(game)
                        continue
                    }
                    spell = spellList.removeFirst()
                    if (spellList.isEmpty()) gameMap.remove(game)
                    if (spellIds.add("${spell.game}-${spell.id}")) break@retry
                }

                spell?.let {
                    result[i] = it
                } ?: run {
                    stars[i] = 3 // 降级处理
                }
            } catch (e: Exception) {
                stars[i] = 3
            }
        }

        // 1-3级卡生成
        for (i in stars.indices) {
            if (result[i] != null) continue

            val requireStar = stars[i]
            val isExMap = map[requireStar] ?: throw HandlerException("${requireStar}星符卡不足")
            val gameMap = isExMap[false] ?: throw HandlerException("${requireStar}星符卡不足")

            var spell: Spell
            do {
                val game = gameMap.keys.randomOrNull(rand) ?: throw HandlerException("${requireStar}星符卡不足")
                val spellList = gameMap[game]!!
                spell = spellList.removeFirst()
                if (spellList.isEmpty()) gameMap.remove(game)
            } while (!spellIds.add("${spell.game}-${spell.id}"))

            result[i] = spell
        }

        for (i in exPos.indices) {
            var index = exPos[i]
            var firstTry = true
            tryOnce@ while (true) {
                if (firstTry) {
                    firstTry = false
                } else {
                    index = (index + 1) % result.size
                    if (index == exPos[i]) throw HandlerException("EX符卡数量不足")
                    if (index in exPos) continue
                }
                val isExMap = map[stars[index]] ?: continue
                val gameMap = isExMap[true] ?: continue
                var spell: Spell
                do {
                    val game = gameMap.keys.randomOrNull(rand) ?: continue@tryOnce
                    val spellList = gameMap[game]!!
                    spell = spellList.removeFirst()
                    if (spellList.isEmpty()) gameMap.remove(game)
                } while (!spellIds.add("${spell.game}-${spell.id}"))
                exPos[i] = index
                result[index] = spell
                break
            }
        }
        return result.filterNotNull().toTypedArray()
    }

    private fun get(type: Int): Map<Int, Map<Boolean, Map<String, List<Spell>>>> {
        val config = cache[type] ?: throw IllegalArgumentException("不支持的比赛类型")
        val files = File(".").listFiles()?.filter { file ->
            file.extension == "xlsx" && !file.name.startsWith("log")
        }?.ifEmpty { null } ?: throw HandlerException("找不到符卡文件")
        var md5sum: HashSet<String>? = hashSetOf()
        files.all { file ->
            val m = md5sum(file.path)
            if (m == null) {
                md5sum = null
                false
            } else {
                md5sum!!.add(m)
                true
            }
        }
        if (md5sum != null && config.md5sum != null && md5sum == config.md5sum)
            return config.allSpells
        val allSpells = HashMap<Int, HashMap<Boolean, HashMap<String, ArrayList<Spell>>>>()
        val spellsByIndex = HashMap<Int, Spell>()
        for (file in files) {
            XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ)).use { wb ->
                val sheet = wb.getSheetAt(0)
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    val spell = config.spellBuilder(row) ?: continue
                    allSpells.getOrPut(spell.star) { hashMapOf() }
                        .getOrPut(spell.rank != "L") { hashMapOf() }
                        .getOrPut(spell.game) { arrayListOf() }
                        .add(spell)
                    spellsByIndex[spell.index] = spell
                }
            }
        }
        config.md5sum = md5sum
        config.allSpells = allSpells
        config.spellsByIndex = spellsByIndex
        SpellLog.createLogFile() // 重读时重新载入log
        return allSpells
    }

    fun getSpellById(type: Int, id: Int): Spell? = cache[type]?.spellsByIndex?.get(id)

    private fun XSSFCell?.getFloatValue(): Float {
        if (this == null) return 0f
        if (cellType == STRING)
            return stringCellValue.ifBlank { return 0f }.toFloat()
        return numericCellValue.toFloat()
    }

    private fun buildNormalSpell(row: XSSFRow): Spell? {
        if (row.lastCellNum < 15) return null
        return Spell(
            index = row.getCell(0).numericCellValue.toInt(),
            game = row.getCell(1).numericCellValue.toInt().toString(),
            name = row.getCell(3).stringCellValue.trim(),
            rank = row.getCell(5).stringCellValue.trim(),
            star = row.getCell(6).numericCellValue.toInt(),
            desc = row.getCell(4)?.stringCellValue?.trim() ?: "",
            id = row.getCell(8)?.numericCellValue?.toInt() ?: 0,
            fastest = row.getCell(9).getFloatValue(),
            one = row.getCell(10).getFloatValue(),
            two = row.getCell(11).getFloatValue(),
            three = row.getCell(12).getFloatValue(),
            final = row.getCell(13).getFloatValue(),
            bonusRate = row.getCell(14).getFloatValue(),
        )
    }

    private fun buildBPSpell(row: XSSFRow): Spell? {
        if (row.lastCellNum < 15) return null
        return Spell(
            index = row.getCell(0).numericCellValue.toInt(),
            game = row.getCell(1).numericCellValue.toInt().toString(),
            name = row.getCell(3).stringCellValue.trim(),
            rank = row.getCell(5).stringCellValue.trim(),
            star = row.getCell(7).numericCellValue.toInt(),
            desc = row.getCell(4)?.stringCellValue?.trim() ?: "",
            id = row.getCell(8).numericCellValue.toInt(),
            fastest = row.getCell(9).getFloatValue(),
            one = row.getCell(10).getFloatValue(),
            two = row.getCell(11).getFloatValue(),
            three = row.getCell(12).getFloatValue(),
            final = row.getCell(13).getFloatValue(),
            bonusRate = row.getCell(14).getFloatValue(),
        )
    }

    private fun md5sum(fileName: String): String? {
        if (isWindows) return null
        val p = Runtime.getRuntime().exec(arrayOf("md5sum", fileName))
        if (!p.waitFor(1, TimeUnit.SECONDS)) {
            logger.error("shell execute failed: md5sum $fileName")
            return null
        }
        p.inputStream.use { `is` ->
            BufferedReader(InputStreamReader(`is`)).use {
                val md5 = it.readLine()
                logger.debug(md5)
                return md5
            }
        }
    }

    private class Config(
        val spellBuilder: (XSSFRow) -> Spell?
    ) {
        var md5sum: Set<String>? = null

        /** star => ( isEx => ( game => spellList ) ) */
        var allSpells: Map<Int, Map<Boolean, Map<String, List<Spell>>>> = mapOf()

        var spellsByIndex: Map<Int, Spell> = mapOf()
    }

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")
}
