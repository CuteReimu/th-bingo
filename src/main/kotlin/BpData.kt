package org.tfcc.bingo
//
// data class BpData(
//    var whoseTurn: Int,
//    var banPick: Int,
//    var round: Int,
//    var lessThan4: Boolean,
//    var spellFailedCountA: IntArray, // 左边玩家符卡失败次数
//    var spellFailedCountB: IntArray, // 右边玩家符卡失败次数
// ) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as BpData
//
//        if (whoseTurn != other.whoseTurn) return false
//        if (banPick != other.banPick) return false
//        if (round != other.round) return false
//        if (lessThan4 != other.lessThan4) return false
//        if (!spellFailedCountA.contentEquals(other.spellFailedCountA)) return false
//        if (!spellFailedCountB.contentEquals(other.spellFailedCountB)) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = whoseTurn
//        result = 31 * result + banPick
//        result = 31 * result + round
//        result = 31 * result + lessThan4.hashCode()
//        result = 31 * result + spellFailedCountA.contentHashCode()
//        result = 31 * result + spellFailedCountB.contentHashCode()
//        return result
//    }
// }
