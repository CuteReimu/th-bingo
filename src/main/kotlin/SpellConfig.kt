package org.tfcc.bingo

import org.apache.log4j.Logger
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
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
    const val NormalGame = 1

    /** BP赛的配置 */
    const val BPGame = 2

    private val cache = mapOf(
        NormalGame to Config(::buildNormalSpell),
        BPGame to Config(::buildBPSpell),
    ) // 因为用的SingleThreadExecutor，因此无需考虑线程安全问题

    /**
     * 随符卡
     * @param type 可以传入 [NormalGame] 或 [BPGame]
     * @param exPos ex符卡的位置
     * @param stars 星级的分布
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
            val isExMap = map[stars[it]] ?: throw HandlerException("符卡数量不足")
            val gameMap = isExMap[false] ?: throw HandlerException("符卡数量不足")
            var spell: Spell
            do {
                val game = gameMap.keys.randomOrNull(rand) ?: throw HandlerException("符卡数量不足")
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
                    if (index == exPos[i]) throw HandlerException("符卡数量不足")
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
        val spellsById = HashMap<Int, Spell>()
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
                    spellsById[spell.id] = spell
                }
            }
        }
        config.md5sum = md5sum
        config.allSpells = allSpells
        config.spellsById = spellsById
        SpellLog.createLogFile() // 重读时重新载入log
        return allSpells
    }

    fun getSpellById(type: Int, id: Int): Spell? = cache[type]?.spellsById?.get(id)

    private fun buildNormalSpell(row: XSSFRow): Spell? {
        if (row.lastCellNum < 6) return null
        return Spell(
            game = row.getCell(1).numericCellValue.toInt().toString(),
            name = row.getCell(3).stringCellValue.trim(),
            rank = row.getCell(5).stringCellValue.trim(),
            star = row.getCell(6).numericCellValue.toInt(),
            desc = row.getCell(4)?.stringCellValue?.trim() ?: "",
            id = if (row.lastCellNum < 8) 0
            else row.getCell(8)?.numericCellValue?.toInt() ?: 0
        )
    }

    private fun buildBPSpell(row: XSSFRow): Spell? {
        if (row.lastCellNum < 7) return null
        return Spell(
            game = row.getCell(1).numericCellValue.toInt().toString(),
            name = row.getCell(3).stringCellValue.trim(),
            rank = row.getCell(5).stringCellValue.trim(),
            star = row.getCell(7).numericCellValue.toInt(),
            desc = row.getCell(4)?.stringCellValue?.trim() ?: "",
            id = if (row.lastCellNum < 8) 0
            else row.getCell(8)?.numericCellValue?.toInt() ?: 0
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

        var spellsById: Map<Int, Spell> = mapOf()
    }

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    private val logger = Logger.getLogger(SpellConfig.javaClass)
}