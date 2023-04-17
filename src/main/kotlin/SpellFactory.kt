package org.tfcc.bingo

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

object SpellFactory {
    /**
     * 随符卡，用于BP赛
     */
    @Throws(HandlerException::class)
    fun randSpellsBP(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
        val lv2Count = 20 - lv1Count
        val lv3Count = 5
        val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
            if (file.extension != "xlsx" || file.name.startsWith("log")) continue
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row.lastCellNum >= 7) {
                    val star = row.getCell(7).numericCellValue.toInt()
                    val inGame = games.contains(row.getCell(1).numericCellValue.toInt().toString()) &&
                            (ranks == null || ranks.contains(row.getCell(5).stringCellValue.trim()))
                    if (star in 1..3 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4)?.stringCellValue ?: "",
                                id = if (row.lastCellNum < 8) 0
                                else row.getCell(8)?.numericCellValue?.toInt() ?: 0
                            )
                        )
                    }
                    if (star == 3 && !inGame) {
                        spells[3].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4)?.stringCellValue ?: "",
                                id = if (row.lastCellNum < 8) 0
                                else row.getCell(8)?.numericCellValue?.toInt() ?: 0
                            )
                        )
                    }
                }
            }
        }
        if (spells[0].size < lv1Count || spells[1].size < lv2Count)
            throw HandlerException("符卡数量不足")
        val rand = ThreadLocalRandom.current()
        spells[0].shuffle(rand)
        spells[1].shuffle(rand)
        val spells01 = ArrayList<Spell>()
        spells01.addAll(spells[0].subList(0, lv1Count))
        spells01.addAll(spells[1].subList(0, lv2Count))
        spells01.shuffle(rand)
        if (spells[2].size < lv3Count) {
            spells[3].shuffle(rand)
            spells[2].addAll(spells[3].subList(0, lv3Count - spells[2].size))
        }
        spells[2].shuffle(rand)
        val idx = intArrayOf(0, 1, 3, 4)
        idx.shuffle(rand.asKotlinRandom())
        var j = 0
        return Array(25) { i ->
            when (i) {
                // 每行、每列都只有一个lv3
                idx[0] -> spells[2][0]
                5 + idx[1] -> spells[2][1]
                12 -> spells[2][2]
                15 + idx[2] -> spells[2][3]
                20 + idx[3] -> spells[2][4]
                else -> spells01[j++]
            }
        }
    }

    /**
     * 随符卡，用于标准模式
     */
    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val allSpells = SpellConfig.get(SpellConfig.NormalGame, games, ranks)
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = Array(lvCount[0]) { "1" } + Array(lvCount[1]) { "2" } + Array(lvCount[2]) { "3" }
        val star45 = arrayOf("4", "4", "4", "4", "5")
        idx.shuffle(rand)
        star45.shuffle(rand)
        star123.shuffle(rand)
        var j = 0
        val gamesAndStars = Array(25) { i ->
            games.random(rand) + "-" +
                    when (i) {
                        // 每行、每列都只有一个大于等于lv4
                        idx[0] -> star45[0]
                        5 + idx[1] -> star45[1]
                        12 -> star45[2]
                        15 + idx[2] -> star45[3]
                        20 + idx[3] -> star45[4]
                        else -> star123[j++]
                    }
        }
        return allSpells.choose(gamesAndStars, rand)
    }

    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpellsLink(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val allSpells = SpellConfig.get(SpellConfig.NormalGame, games, ranks)
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = Array(lvCount[0]) { "1" } + Array(lvCount[1]) { "2" } + Array(lvCount[2]) { "3" }
        idx.shuffle(rand)
        star123.shuffle(rand)
        var j = 0
        val gamesAndStars = Array(25) { i ->
            games.random(rand) + "-" +
                    when (i) {
                        0, 4 -> "1" // 左上lv1，右上lv1
                        6, 8, 16, 18 -> "4" // 第二、四排的第二、四列固定4级
                        12 -> "5" // 中间5级
                        else -> star123[j++]
                    }
        }
        return allSpells.choose(gamesAndStars, rand)
    }

    private fun Map<String, List<Spell>>.choose(keys: Array<String>, rand: Random): Array<Spell> {
        var failedTimes = 0
        val exists = HashSet<String>() // "$game-$id"
        return Array(keys.size) {
            val spells = this[keys[it]] ?: throw SpellNotEnoughException()
            while (failedTimes < 100) {
                val spell = spells.randomOrNull(rand) ?: throw SpellNotEnoughException()
                if (exists.add("${spell.game}-${spell.id}"))
                    return@Array spell
                failedTimes++
            }
            throw SpellNotEnoughException()
        }
    }

    internal class SpellNotEnoughException : HandlerException("符卡数量不足")
}