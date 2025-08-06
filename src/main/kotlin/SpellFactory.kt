package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.asKotlinRandom

object SpellFactory {
    /**
     * 随符卡，用于BP赛
     */
    @Throws(HandlerException::class)
    fun randSpellsBP(games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
        val lv2Count = 20 - lv1Count
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star12 = IntArray(lv1Count) { 1 } + IntArray(lv2Count) { 2 }
        idx.shuffle(rand)
        star12.shuffle(rand)
        var j = 0
        // 每行、每列都只有一个lv3
        val idx3 = intArrayOf(idx[0], 5 + idx[1], 12, 15 + idx[2], 20 + idx[3])
        val stars = IntArray(25) { i -> if (i in idx3) 3 else star12[j++] }
        return SpellConfig.get(SpellConfig.BP_GAME, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于标准模式
     */
    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = IntArray(lvCount[0]) { 1 } + IntArray(lvCount[1]) { 2 } + IntArray(lvCount[2]) { 3 }
        val star45 = intArrayOf(4, 4, 4, 4, 5)
        idx.shuffle(rand)
        star45.shuffle(rand)
        star123.shuffle(rand)
        var j = 0
        val stars = IntArray(25) { i ->
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
        return SpellConfig.get(SpellConfig.NORMAL_GAME, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于双面模式
     */
    @Throws(HandlerException::class)
    fun randSpellsDualPage(
        games: Array<String>,
        ranks: Array<String>?,
        difficulty: Difficulty,
        diffLevel: Int,
        firstPageStars: IntArray
    ): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = IntArray(lvCount[0]) { 1 } + IntArray(lvCount[1]) { 2 } + IntArray(lvCount[2]) { 3 }
        val star45 = intArrayOf(4, 4, 4, 4, 5)
        val l2h = star123 + star45
        val h2l = star123 + star45
        l2h.sort()
        h2l.sortDescending()
        fun calDiff(stars1: IntArray, stars2: IntArray): Int {
            var diff = 0
            for (i in stars1.indices) {
                diff += abs(stars1[i] - stars2[i])
            }
            return diff
        }
        val maxDiff = calDiff(l2h, h2l)
        idx.shuffle(rand)
        star45.shuffle(rand)
        star123.shuffle(rand)
        var stars: IntArray? = null
        for (i in 0 until 100) {
            var j = 0
            stars = IntArray(25) { i ->
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
            val diffLevel2 = calDiff(stars, firstPageStars) / maxDiff
            if (abs(diffLevel2 - diffLevel) <= 0.5) break
            stars = null
        }
        stars != null || throw HandlerException("生成双面符卡失败")
        return SpellConfig.get(SpellConfig.NORMAL_GAME, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpellsLink(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = IntArray(lvCount[0]) { 1 } + IntArray(lvCount[1]) { 2 } + IntArray(lvCount[2]) { 3 }
        idx.shuffle(rand)
        star123.shuffle(rand)
        var j = 0
        val stars = IntArray(25) { i ->
            when (i) {
                0, 4 -> 1 // 左上lv1，右上lv1
                6, 8, 16, 18 -> 4 // 第二、四排的第二、四列固定4级
                12 -> 5 // 中间5级
                else -> star123[j++]
            }
        }
        return SpellConfig.get(SpellConfig.NORMAL_GAME, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    private fun ranksToExPos(ranks: Array<String>?, rand: Random): IntArray {
        if (ranks != null && ranks.all { it == "L" })
            return intArrayOf()
        val idx = intArrayOf(0, 1, 2, 3, 4)
        idx.shuffle(rand)
        for ((i, j) in idx.withIndex())
            idx[i] = i * 5 + j
        return idx
    }
}
