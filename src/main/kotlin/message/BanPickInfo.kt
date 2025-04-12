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
) {
    // 服务器中，A固定是先手玩家，B固定是后手玩家，方便计算。
    // 发给客户端时，A固定是左边玩家，B固定是右边玩家，会根据whoFirst值决定是否需要颠倒AB
    operator fun not(): BanPickInfo {
        return BanPickInfo(
            whoFirst,
            phase,
            bPick,
            bBan,
            aPick,
            aBan,
            bOpenEx,
            aOpenEx
        )
    }
}
