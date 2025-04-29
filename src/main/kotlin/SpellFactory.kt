package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

object SpellFactory {
    /**
     * 随符卡，用于BP赛
     */
    @Throws(HandlerException::class)
    fun randSpellsBP(spellCardVersion: Int, games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
        val lv2Count = 20 - lv1Count
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star12 = IntArray(lv1Count) { 1 } + IntArray(lv2Count) { 2 }
        idx.shuffle(rand)
        star12.shuffle(rand)
        var j = 0
        // 每行、每列都只有一个lv3
        val idx3 = arrayOf(idx[0], 5 + idx[1], 12, 15 + idx[2], 20 + idx[3])
        val stars = IntArray(25) { i -> if (i in idx3) 3 else star12[j++] }
        return SpellConfig.get(SpellConfig.BP_GAME, spellCardVersion, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于BP赛OD
     */
    @Throws(HandlerException::class)
    fun randSpellsBPOD(spellCardVersion: Int, games: Array<String>, ranks: Array<String>?, lv1Count: Int): Array<Spell> {
        val lv2Count = 15 - lv1Count
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star12 = IntArray(lv1Count) { 1 } + IntArray(lv2Count) { 2 } + IntArray(5) { 13 }
        idx.shuffle(rand)
        star12.shuffle(rand)
        var j = 0
        // 每行、每列都只有一个lv3
        val idx3 = arrayOf(idx[0], 5 + idx[1], 12, 15 + idx[2], 20 + idx[3])
        val stars = IntArray(25) { i -> if (i in idx3) 3 else star12[j++] }
        return SpellConfig.getBPOD(SpellConfig.BP_GAME, spellCardVersion, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于标准模式
     * difficulty：123级卡数量 int[]
     * games：开启的游戏 string[]
     *
     */
    @Throws(HandlerException::class)
    fun randSpells(spellCardVersion: Int, games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        val lvCount = difficulty.value
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star123 = IntArray(lvCount[0]) { 1 } + IntArray(lvCount[1]) { 2 } + IntArray(lvCount[2]) { 3 }
        val star45 = arrayOf(4, 4, 4, 4, 5)
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
        return SpellConfig.get(SpellConfig.NORMAL_GAME, spellCardVersion, games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于标准模式-OD
     * difficulty：123级卡数量 int[]
     * games：开启的游戏 string[]
     *
     */
    @Throws(HandlerException::class)
    fun randSpellsOD(spellCardVersion: Int, games: Array<String>, ranks: Array<String>?): Array<Spell> {
        // 14=四级替换 15=五级替换
        // (1,4,8,9,3)
        val lvCount = arrayOf(1, 4, 8, 5, 2)
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        val idx = intArrayOf(0, 1, 3, 4)
        val star12367 = IntArray(lvCount[0]) { 1 } + IntArray(lvCount[1]) { 2 } + IntArray(lvCount[2]) { 3 } +
            IntArray(lvCount[3]) { 14 } + IntArray(lvCount[4]) { 15 }
        val star45 = arrayOf(4, 4, 4, 4, 5)
        idx.shuffle(rand)
        star45.shuffle(rand)
        star12367.shuffle(rand)
        var j = 0
        val stars = IntArray(25) { i ->
            when (i) {
                // 每行、每列都只有一个大于等于lv4
                idx[0] -> star45[0]
                5 + idx[1] -> star45[1]
                12 -> star45[2]
                15 + idx[2] -> star45[3]
                20 + idx[3] -> star45[4]
                else -> star12367[j++]
            }
        }
        return SpellConfig.getOD(SpellConfig.NORMAL_GAME, spellCardVersion,
            games, ranks, ranksToExPos(ranks, rand), stars, rand)
    }

    /**
     * 随符卡，用于link赛
     */
    @Throws(HandlerException::class)
    fun randSpellsLink(
        spellCardVersion: Int,
        games: Array<String>,
        ranks: Array<String>?,
        difficulty: Difficulty
    ): Array<Spell> {
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
        return SpellConfig.get(SpellConfig.NORMAL_GAME, spellCardVersion, games, ranks, ranksToExPos(ranks, rand), stars, rand)
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
