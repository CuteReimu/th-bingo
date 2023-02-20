package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import java.io.Serializable

data class Room(
    val roomId: String,
    var roomType: Int,
    var host: String,
    var players: Array<String>?,
    var started: Boolean,
    var spells: Array<Spell>?,
    var startMs: Long,
    var gameTime: Int, // 比赛时长，分
    var countDown: Int, // 倒计时，秒
    var spellStatus: Array<SpellStatus>?, // 每个格子的状态
    var score: IntArray?, // 比分
    var locked: Boolean, // 连续多局就需要锁上
    var needWin: Int, // 需要赢几局才算赢
    var changeCardCount: IntArray?,
    var totalPauseMs: Long, // 累计暂停时长，毫秒
    var pauseBeginMs: Long, // 开始暂停时刻，毫秒，0表示没暂停
    var lastWinner: Int, // 上一场是谁赢，1或2
    var bpData: BpData?,
    var linkData: LinkData?,
    var phase: Int, // 纯客户端用，服务器只记录
    var lastOperateMs: Long = 0 // 最后一次操作的时间戳，毫秒
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
        if (bpData != other.bpData) return false
        if (linkData != other.linkData) return false
        if (phase != other.phase) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + roomType
        result = 31 * result + host.hashCode()
        result = 31 * result + (players?.contentHashCode() ?: 0)
        result = 31 * result + started.hashCode()
        result = 31 * result + (spells?.contentHashCode() ?: 0)
        result = 31 * result + startMs.hashCode()
        result = 31 * result + gameTime
        result = 31 * result + countDown
        result = 31 * result + (spellStatus?.contentHashCode() ?: 0)
        result = 31 * result + (score?.contentHashCode() ?: 0)
        result = 31 * result + locked.hashCode()
        result = 31 * result + needWin
        result = 31 * result + (changeCardCount?.contentHashCode() ?: 0)
        result = 31 * result + totalPauseMs.hashCode()
        result = 31 * result + pauseBeginMs.hashCode()
        result = 31 * result + lastWinner
        result = 31 * result + (bpData?.hashCode() ?: 0)
        result = 31 * result + (linkData?.hashCode() ?: 0)
        result = 31 * result + phase
        return result
    }

    val type
        get() = when (roomType) {
            1 -> RoomTypeNormal
            2 -> RoomTypeBP
            3 -> RoomTypeLink
            else -> throw HandlerException("不支持的游戏类型")
        }
}
