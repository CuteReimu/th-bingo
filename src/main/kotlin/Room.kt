package org.tfcc.bingo

import java.io.Serializable

data class Room(
    val roomId: String,
    var roomType: Int?,
    var host: String?,
    var players: Array<String>?,
    var started: Boolean?,
    var spells: Array<Spell>?,
    var startMs: Long?,
    var gameTime: Int?, // 比赛时长，分
    var countDown: Int?, // 倒计时，秒
    var spellStatus: Array<SpellStatus>?, // 每个格子的状态
    var score: Int?, // 比分
    var locked: Boolean?, // 连续多局就需要锁上
    var needWin: Int?, // 需要赢几局才算赢
    var changeCardCount: Array<Int>?,
    var totalPauseMs: Long?, // 累计暂停时长，毫秒
    var pauseBeginMs: Long?, // 开始暂停时刻，毫秒，0表示没暂停
    var lastWinner: Int?, // 上一场是谁赢，1或2
    var phase: Int? // 纯客户端用，服务器只记录
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        return roomId.hashCode()
    }
}
