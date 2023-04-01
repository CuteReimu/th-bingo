package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

class Room(
    val roomId: String,
    var roomType: Int,
    val host: String,
    val players: Array<String>,
    var started: Boolean,
    var spells: Array<Spell>?,
    var startMs: Long,
    var gameTime: Int, // 比赛时长，分
    var countDown: Int, // 倒计时，秒
    var spellStatus: Array<SpellStatus>?, // 每个格子的状态
    val score: IntArray, // 比分
    var locked: Boolean, // 连续多局就需要锁上
    var needWin: Int, // 需要赢几局才算赢
    val changeCardCount: IntArray,
    var totalPauseMs: Long, // 累计暂停时长，毫秒
    var pauseBeginMs: Long, // 开始暂停时刻，毫秒，0表示没暂停
    var lastWinner: Int, // 上一场是谁赢，1或2
    var bpData: BpData?,
    var linkData: LinkData?,
    var phase: Int, // 纯客户端用，服务器只记录
    var lastOperateMs: Long = 0, // 最后一次操作的时间戳，毫秒
    val watchers: ArrayList<String>, // 观众
    var difficulty: Int,
    var enableTools: Boolean
) {
    val type
        get() = when (roomType) {
            1 -> RoomTypeNormal
            2 -> RoomTypeBP
            3 -> RoomTypeLink
            else -> throw HandlerException("不支持的游戏类型")
        }
}
