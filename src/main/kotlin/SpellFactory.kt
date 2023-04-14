package org.tfcc.bingo

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ThreadLocalRandom
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
        val lvCount = difficulty.value + arrayOf(4, 1)
        val rand = ThreadLocalRandom.current()
        val allSpells = SpellConfig.get(SpellConfig.NormalGame, games, ranks).shuffled(rand)
        // 打乱后去除重复符卡
        val starToSpells = allSpells.distinctBy { "${it.game}-${it.id}" }.groupBy { it.star }
        val spells = Array(5) { starToSpells[it + 1] ?: listOf() }
        if (spells.zip(lvCount).any { (it, size) -> it.size < size })
            throw HandlerException("符卡数量不足")

        val easySpell = (0..2).flatMap { i -> spells[i].subList(0, lvCount[i]) }.shuffled(rand)
        val topSpell = (3..4).flatMap { i -> spells[i].subList(0, lvCount[i]) }.shuffled(rand)

        val idx = intArrayOf(0, 1, 3, 4)
        idx.shuffle(rand.asKotlinRandom())
        var j = 0
        return Array(25) { i ->
            when (i) {
                // 每行、每列都只有一个大于等于lv4
                idx[0] -> topSpell[0]
                5 + idx[1] -> topSpell[1]
                12 -> topSpell[2]
                15 + idx[2] -> topSpell[3]
                20 + idx[3] -> topSpell[4]
                else -> easySpell[j++]
            }
        }
    }

    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpellsLink(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value + arrayOf(4, 1)
        val rand = ThreadLocalRandom.current()
        val allSpells = SpellConfig.get(SpellConfig.NormalGame, games, ranks).shuffled(rand)
        // 打乱后去除重复符卡
        val starToSpells = allSpells.distinctBy { "${it.game}-${it.id}" }.groupBy { it.star }
        val spells = Array(5) { starToSpells[it + 1] ?: listOf() }
        if (spells.zip(lvCount).any { (it, size) -> it.size < size })
            throw HandlerException("符卡数量不足")
        val s00 = spells[0][0] // 左上lv1
        val s04 = spells[0][4] // 右上lv1
        val easySpell = ArrayList<Spell>()
        easySpell.addAll(spells[0].subList(2, lvCount[0]))
        easySpell.addAll(spells[1].subList(0, lvCount[1]))
        easySpell.addAll(spells[2].subList(0, lvCount[2]))
        easySpell.shuffle(rand)

        // 第二、四排的第二、四列固定4级
        val s22 = spells[4][0] // 中间5级
        val s11 = spells[3][0]
        val s13 = spells[3][1]
        val s31 = spells[3][2]
        val s33 = spells[3][3]
        var j = 0
        return Array(25) { i ->
            when (i) {
                0 -> s00
                4 -> s04
                6 -> s11
                8 -> s13
                12 -> s22
                16 -> s31
                18 -> s33
                else -> easySpell[j++]
            }
        }
    }
}