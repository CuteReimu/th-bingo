package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData
import org.tfcc.bingo.Spell

data class SpellListSc(
    val spells: Array<Spell>,
    val time: Long,
    val startTime: Long,
    val gameTime: Int, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int, // 倒计时，单位：秒
    val needWin: Int,
    val whoseTurn: Int,
    val banPick: Int,
    val totalPauseTime: Long?,
    val pauseBeginMs: Long?,
    val status: IntArray?,
    val phase: Int,
    val linkData: LinkData?,
    val difficulty: Int,
    val enableTools: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpellListSc

        if (!spells.contentEquals(other.spells)) return false
        if (time != other.time) return false
        if (startTime != other.startTime) return false
        if (gameTime != other.gameTime) return false
        if (countdown != other.countdown) return false
        if (needWin != other.needWin) return false
        if (whoseTurn != other.whoseTurn) return false
        if (banPick != other.banPick) return false
        if (totalPauseTime != other.totalPauseTime) return false
        if (pauseBeginMs != other.pauseBeginMs) return false
        if (!status.contentEquals(other.status)) return false
        if (phase != other.phase) return false
        if (linkData != other.linkData) return false
        if (difficulty != other.difficulty) return false
        if (enableTools != other.enableTools) return false

        return true
    }

    override fun hashCode(): Int {
        var result = spells.contentHashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + gameTime.hashCode()
        result = 31 * result + countdown.hashCode()
        result = 31 * result + needWin.hashCode()
        result = 31 * result + whoseTurn
        result = 31 * result + banPick
        result = 31 * result + (totalPauseTime?.hashCode() ?: 0)
        result = 31 * result + (pauseBeginMs?.hashCode() ?: 0)
        result = 31 * result + status.contentHashCode()
        result = 31 * result + phase
        result = 31 * result + linkData.hashCode()
        result = 31 * result + difficulty
        result = 31 * result + enableTools.hashCode()
        return result
    }
}
