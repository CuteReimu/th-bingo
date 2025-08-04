package org.tfcc.bingo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Spell(
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
    @SerialName("bonus_rate")
    val bonusRate: Float,
    @SerialName("is_transition")
    val isTransition: Boolean = false,
)
