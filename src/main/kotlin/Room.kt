package org.tfcc.bingo

import java.io.Serializable

data class Room(
    val roomId: String,
    val roomType: Int,
    val host: String,
    val players: Array<String>?,
    val started: Boolean?,
    val spells: Array<Spell>?,
    val startMs: Long?,
    val gameTime: Int?, // 比赛时长，分
    val countDown: Int?, // 倒计时，秒
    val spellStatus: Array<SpellStatus>?, // 每个格子的状态
    val score: Array<UInt>?, // 比分
    val locked: Boolean?, // 连续多局就需要锁上
    val needWin: Int?, // 需要赢几局才算赢
    val changeCardCount: Array<UInt>?,
    val totalPauseMs: Long?, // 累计暂停时长，毫秒
    val pauseBeginMs: Long?, // 开始暂停时刻，毫秒，0表示没暂停
    val lastWinner: Int?, // 上一场是谁赢，1或2
    val phase: Int? // 纯客户端用，服务器只记录
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (roomId != other.roomId) return false
        if (roomType != other.roomType) return false
        if (host != other.host) return false
        if (players != null) {
            if (other.players == null) return false
            if (!players.contentEquals(other.players)) return false
        } else if (other.players != null) return false
        if (started != other.started) return false
        if (spells != null) {
            if (other.spells == null) return false
            if (!spells.contentEquals(other.spells)) return false
        } else if (other.spells != null) return false
        if (startMs != other.startMs) return false
        if (gameTime != other.gameTime) return false
        if (countDown != other.countDown) return false
        if (spellStatus != null) {
            if (other.spellStatus == null) return false
            if (!spellStatus.contentEquals(other.spellStatus)) return false
        } else if (other.spellStatus != null) return false
        if (score != null) {
            if (other.score == null) return false
            if (!score.contentEquals(other.score)) return false
        } else if (other.score != null) return false
        if (locked != other.locked) return false
        if (needWin != other.needWin) return false
        if (changeCardCount != null) {
            if (other.changeCardCount == null) return false
            if (!changeCardCount.contentEquals(other.changeCardCount)) return false
        } else if (other.changeCardCount != null) return false
        if (totalPauseMs != other.totalPauseMs) return false
        if (pauseBeginMs != other.pauseBeginMs) return false
        if (lastWinner != other.lastWinner) return false
        if (phase != other.phase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + roomType
        result = 31 * result + host.hashCode()
        result = 31 * result + (players?.contentHashCode() ?: 0)
        result = 31 * result + (started?.hashCode() ?: 0)
        result = 31 * result + (spells?.contentHashCode() ?: 0)
        result = 31 * result + (startMs?.hashCode() ?: 0)
        result = 31 * result + (gameTime ?: 0)
        result = 31 * result + (countDown ?: 0)
        result = 31 * result + (spellStatus?.contentHashCode() ?: 0)
        result = 31 * result + (score?.contentHashCode() ?: 0)
        result = 31 * result + (locked?.hashCode() ?: 0)
        result = 31 * result + (needWin ?: 0)
        result = 31 * result + (changeCardCount?.contentHashCode() ?: 0)
        result = 31 * result + (totalPauseMs?.hashCode() ?: 0)
        result = 31 * result + (pauseBeginMs?.hashCode() ?: 0)
        result = 31 * result + (lastWinner ?: 0)
        result = 31 * result + (phase ?: 0)
        return result
    }
}
