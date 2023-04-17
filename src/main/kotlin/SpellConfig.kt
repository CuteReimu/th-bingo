package org.tfcc.bingo

import org.apache.log4j.Logger
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

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
     * 获取符卡相关的xlsx配置
     * @param type 可以传入 [NormalGame] 或 [BPGame]
     */
    fun get(type: Int, games: Array<String>, ranks: Array<String>?): List<Spell> =
        get(type).filter { spell -> games.contains(spell.game) && (ranks == null || ranks.contains(spell.rank)) }

    private fun get(type: Int): List<Spell> {
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
        val allSpells = ArrayList<Spell>()
        for (file in files) {
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                val spell = config.spellBuilder(row) ?: continue
                allSpells.add(spell)
            }
        }
        config.md5sum = md5sum
        config.allSpells = allSpells
        return allSpells
    }

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
        if (row.lastCellNum < 6) return null
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
        var allSpells: List<Spell> = listOf()
    }

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    private val logger = Logger.getLogger(SpellConfig.javaClass)
}