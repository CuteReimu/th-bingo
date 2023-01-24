package org.tfcc.bingo

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.asKotlinRandom

object SpellFactory {
    /**
     * 随符卡，用于标准模式、BP赛
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
                    val star = row.getCell(6).rawValue.toInt()
                    val inGame = games.contains(row.getCell(1).rawValue.trim()) &&
                            (ranks == null || ranks.contains(row.getCell(5).rawValue.trim()))
                    if (star in 1..3 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).rawValue,
                                name = row.getCell(3).rawValue,
                                rank = row.getCell(5).rawValue,
                                star = star,
                                desc = row.getCell(4).rawValue
                            )
                        )
                    }
                    if (star == 3 && !inGame) {
                        spells[3].add(
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
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpells2(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
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
                    val star = row.getCell(6).rawValue.toInt()
                    val inGame = games.contains(row.getCell(1).rawValue.trim()) &&
                            (ranks == null || ranks.contains(row.getCell(5).rawValue.trim()))
                    if (star in 1..3 && inGame) {
                        spells[star - 1].add(
                            Spell(
                                game = row.getCell(1).rawValue,
                                name = row.getCell(3).rawValue,
                                rank = row.getCell(5).rawValue,
                                star = star,
                                desc = row.getCell(4).rawValue
                            )
                        )
                    }
                    if (star == 3 && !inGame) {
                        spells[3].add(
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
        val s00 = spells[0][0] // 左上lv1
        val s04 = spells[0][4] // 右上lv1
        val spells01 = ArrayList<Spell>()
        spells01.addAll(spells[0].subList(2, lv1Count))
        spells01.addAll(spells[1].subList(0, lv2Count))
        spells01.shuffle(rand)
        // 第二、四排的第二、四列是lv1或lv2
        val s11 = spells01[0]
        val s13 = spells01[1]
        val s31 = spells01[2]
        val s33 = spells01[3]
        val spells012 = ArrayList(spells01.subList(4, spells01.size))
        if (spells[2].size < lv3Count) {
            spells[3].shuffle(rand)
            spells[2].addAll(spells[3].subList(0, lv3Count - spells[2].size))
        }
        spells[2].shuffle(rand)
        val s22 = spells[2][0] // 中间lv3
        val s40 = spells[2][1] // 左下lv3
        val s44 = spells[2][2] // 右下lv3
        spells012.addAll(spells[2].subList(3, spells[2].size))
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