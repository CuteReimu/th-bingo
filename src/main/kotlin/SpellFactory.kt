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
    fun randSpells(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell?>? {
        val lv2Count = 20 - lv1Count
        val lv3Count = 5
        val files = File(".").listFiles() ?: return null
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
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
        val result = Array<Spell?>(25) { _ -> null }
        // 每行、每列都只有一个lv3
        result[idx[0]] = spells[2][0]
        result[5 + idx[1]] = spells[2][1]
        result[12] = spells[2][2]
        result[15 + idx[2]] = spells[2][3]
        result[20 + idx[3]] = spells[2][4]
        var j = 0
        for (i in result.indices) {
            if (result[i] == null) {
                result[i] = spells01[j++]
            }
        }
        return result
    }


    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpells2(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell?>? {
        val lv2Count = 20 - lv1Count
        val lv3Count = 5
        val files = File(".").listFiles() ?: return null
        val spells = Array(4) { _ -> ArrayList<Spell>() }
        for (file in files) {
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
        var spells01 = ArrayList<Spell>()
        spells01.addAll(spells[0].subList(0, lv1Count))
        spells01.addAll(spells[1].subList(0, lv2Count))
        spells01.shuffle(rand)
        if (spells[2].size < lv3Count) {
            spells[3].shuffle(rand)
            spells[2].addAll(spells[3].subList(0, lv3Count - spells[2].size))
        }
        spells[2].shuffle(rand)
        val result = Array<Spell?>(25) { _ -> null }
        result[0] = spells01[0] // 左上lv1
        result[4] = spells01[1] // 右上lv1
        result[12] = spells[2][0] // 中间lv3
        result[20] = spells[2][1] // 左下lv3
        result[24] = spells[2][2] // 右下lv3
        spells01 = spells01.subList(2, spells01.size) as ArrayList<Spell>
        // 第二、四排的第二、四列是lv1或lv2
        spells01.shuffle(rand) // 打乱lv1和lv2
        result[6] = spells01[0]
        result[8] = spells01[1]
        result[16] = spells01[2]
        result[18] = spells01[3]
        var j = 4
        for (i in result.indices) {
            if (result[i] == null) {
                result[i] = spells01[j++]
            }
        }
        return result
    }
}