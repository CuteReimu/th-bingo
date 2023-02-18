package org.tfcc.bingo

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import kotlin.random.asKotlinRandom

object SpellFactory {
    /**
     * 随符卡，用于BP赛
     */
    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
        val lv2Count = 20 - lv1Count
        val lv3Count = 5
        val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
            if (file.extension != "xlsx") continue
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row.lastCellNum >= 6) {
                    val star = row.getCell(6).numericCellValue.toInt()
                    val inGame = games.contains(row.getCell(1).numericCellValue.toInt().toString()) &&
                            (ranks == null || ranks.contains(row.getCell(5).stringCellValue.trim()))
                    if (star in 1..3 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4).stringCellValue
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
                                desc = row.getCell(4).stringCellValue
                            )
                        )
                    }
                }
            }
        }
        if (spells[0].size < lv1Count || spells[1].size < lv2Count || spells[2].size < lv3Count)
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
        val lv1Count = difficulty.value[0]
        val lv2Count = difficulty.value[1]
        val lv3Count = difficulty.value[2]
        val lv5Count = 1
        val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
            if (file.extension != "xlsx") continue
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row.lastCellNum >= 6) {
                    val star = row.getCell(6).numericCellValue.toInt()
                    val inGame = games.contains(row.getCell(1).numericCellValue.toInt().toString()) &&
                            (ranks == null || ranks.contains(row.getCell(5).stringCellValue.trim()))
                    if (star in 1..3 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4).stringCellValue
                            )
                        )
                    }
                    // 5级卡可以跨作且固定一个
                    if (star == 5 && !inGame) {
                        spells[5].add(
                            Spell(
                                game = row.getCell(1).rawValue,
                                name = row.getCell(3).rawValue,
                                rank = row.getCell(5).rawValue,
                                star = star,
                                desc = row.getCell(4).rawValue
                            )
                        )
                    }
                }
            }
        }
        if (spells[0].size < lv1Count || spells[1].size < lv2Count || spells[2].size < lv3Count)
            throw HandlerException("符卡数量不足")
        val rand = ThreadLocalRandom.current()
        spells[0].shuffle(rand)
        spells[1].shuffle(rand)
        spells[2].shuffle(rand)
        val spells01 = ArrayList<Spell>()
        spells01.addAll(spells[0].subList(0, lv1Count))
        spells01.addAll(spells[1].subList(0, lv2Count))
        spells01.addAll(spells[2].subList(0, lv3Count))
        spells01.shuffle(rand)

        val topSpell = ArrayList<Spell>()
        spells[3].shuffle(rand)
        // 当5级卡不足时，跨作品填充5级卡
        if (spells[4].size < lv5Count){
            spells[5].shuffle(rand)
            spells[4].addAll(spells[5].subList(0, lv5Count - spells[4].size))
        }
        spells[4].shuffle(rand)
        topSpell.addAll(spells[3].subList(0, 4))
        topSpell.add(spells[4][0])
        topSpell.shuffle(rand)
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
                else -> spells01[j++]
            }
        }
    }

    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpellsLink(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lv1Count = difficulty.value[0]
        val lv2Count = difficulty.value[1]
        val lv3Count = difficulty.value[2]
        val topSpell = 5
        val lv5Count = 1
        val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
            if (file.extension != "xlsx") continue
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row.lastCellNum >= 6) {
                    val star = row.getCell(6).numericCellValue.toInt()
                    val inGame = games.contains(row.getCell(1).numericCellValue.toInt().toString()) &&
                            (ranks == null || ranks.contains(row.getCell(5).stringCellValue.trim()))
                    if (star in 1..5 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4).stringCellValue
                            )
                        )
                    }
                    if (star == 5 && !inGame) {
                        spells[5].add(
                            Spell(
                                game = row.getCell(1).numericCellValue.toInt().toString(),
                                name = row.getCell(3).stringCellValue,
                                rank = row.getCell(5).stringCellValue,
                                star = star,
                                desc = row.getCell(4).stringCellValue
                            )
                        )
                    }
                }
            }
        }
        if (spells[0].size < lv1Count || spells[1].size < lv2Count || spells[2].size < lv3Count)
            throw HandlerException("符卡数量不足")
        val rand = ThreadLocalRandom.current()
        spells[0].shuffle(rand)
        spells[1].shuffle(rand)
        val s00 = spells[0][0] // 左上lv1
        val s04 = spells[0][4] // 右上lv1
        val spells01 = ArrayList<Spell>()
        spells01.addAll(spells[0].subList(2, lv1Count))
        spells01.addAll(spells[1].subList(0, lv2Count))
        spells01.shuffle(rand)
        // 第二、四排的第二、四列不出现4和5级
        val s11 = spells01[0]
        val s13 = spells01[1]
        val s31 = spells01[2]
        val s33 = spells01[3]
        val spells012 = ArrayList(spells01.subList(4, spells01.size))

        val topSpellList = ArrayList<Spell>()
        if (spells[4].size < lv5Count) {
            spells[5].shuffle(rand)
            spells[4].addAll(spells[5].subList(0, lv5Count - spells[4].size))
        }
        topSpellList.addAll(spells[3])
        topSpellList.addAll(spells[4])
        topSpellList.shuffle(rand)

        val s22 = topSpellList[0] // 中间4，5级
        val s40 = topSpellList[1] // 左下4，5级
        val s44 = topSpellList[2] // 右下4，5级
        spells012.addAll(topSpellList.subList(topSpell - 2, topSpellList.size))
        spells012.shuffle(rand) // 打乱lv1和lv2和lv3
        var j = 4
        return Array(25) { i ->
            when (i) {
                0 -> s00
                4 -> s04
                6 -> s11
                8 -> s13
                12 -> s22
                16 -> s31
                18 -> s33
                20 -> s40
                24 -> s44
                else -> spells012[j++]
            }
        }
    }
}