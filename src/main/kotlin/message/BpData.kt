package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class BpData(
    @SerialName("whose_turn")
    var whoseTurn: Int,
    @SerialName("ban_pick")
    var banPick: Int,
) {
    @Transient
    var round: Int = 0

    @Transient
    var lessThan4 = false

    /** 左边玩家符卡失败次数 */
    @SerialName("spell_failed_count_a")
    var spellFailedCountA = IntArray(25)

    /** 右边玩家符卡失败次数 */
    @SerialName("spell_failed_count_b")
    var spellFailedCountB = IntArray(25)
}
