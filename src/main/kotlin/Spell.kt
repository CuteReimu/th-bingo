package org.tfcc.bingo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Spell(
    val index: Int,
    val game: String,
    val name: String,
    val rank: String,
    val star: Int,
    val desc: String,
    val id: Int,
    val fastest: Float,
    val one: Float,
    val two: Float,
    val three: Float,
    val final: Float,
    @SerialName("bonus_rate")
    val bonusRate: Float,
    @SerialName("is_transition")
    var isTransition: Boolean = false,
)
