package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param banPick 0-选，1-ban
 */
@Serializable
class NormalData {

    /** 玩家是处于盘A还是盘B */
    @SerialName("which_board_a")
    var whichBoardA = 0

    @SerialName("which_board_b")
    var whichBoardB = 0

    /** A盘面的传送门下标 */
    @SerialName("is_portal_a")
    val isPortalA = Array(25) { 0 }

    @SerialName("is_portal_b")
    val isPortalB = Array(25) { 0 }

    // 0x1: Left在A面收取 0x2: Left, B; Right = Left << 4
    @SerialName("get_on_which_board")
    val getOnWhichBoard = Array(25) { 0 }
}
