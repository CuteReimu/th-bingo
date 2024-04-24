package org.tfcc.bingo

import java.io.Serializable

data class Spell(
    val index: Int,
    val game: String,
    val name: String,
    var rank: String,
    var star: Int,
    var desc: String,
    val id: Int,
    val fastest: Float,
    val one: Float,
    val two: Float,
    val three: Float,
    val final: Float,
    val bonusRate: Float,
) : Serializable
