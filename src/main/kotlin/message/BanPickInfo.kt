package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BanPickInfo(
    @SerialName("who_first")
    val whoFirst: Int,
    val phase: Int,
    @SerialName("a_pick")
    val aPick: Array<String>,
    @SerialName("a_ban")
    val aBan: Array<String>,
    @SerialName("b_pick")
    val bPick: Array<String>,
    @SerialName("b_ban")
    val bBan: Array<String>,
    @SerialName("a_open_ex")
    val aOpenEx: Int,
    @SerialName("b_open_ex")
    val bOpenEx: Int,
)
