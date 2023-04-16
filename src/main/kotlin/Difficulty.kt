package org.tfcc.bingo

import kotlin.random.Random

class Difficulty(val value: IntArray) {
    companion object {
        val E = Difficulty(intArrayOf(12, 6, 2))
        val N = Difficulty(intArrayOf(6, 8, 6))
        val L = Difficulty(intArrayOf(2, 6, 12))

        fun random(): Difficulty {
            val weights = intArrayOf(11, 10, 9)
            val n = Random.nextInt(weights.sum()).let {
                if (it < weights[0]) 6
                else if (it < weights[0] + weights[1]) 7
                else 8
            }
            val e = (2..(18 - n)).random()
            return Difficulty(intArrayOf(e, n, 20 - e - n))
        }
    }
}