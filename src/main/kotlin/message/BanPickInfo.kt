package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BanPickInfo(
    @SerialName("who_first")
    val whoFirst: Int,
    val phase: Int,
    @SerialName("a_pick")
    val aPick: List<String>,
    @SerialName("a_ban")
    val aBan: List<String>,
    @SerialName("b_pick")
    val bPick: List<String>,
    @SerialName("b_ban")
    val bBan: List<String>,
    @SerialName("a_open_ex")
    val aOpenEx: Int,
    @SerialName("b_open_ex")
    val bOpenEx: Int,
)
