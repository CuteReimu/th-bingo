package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

class Room(
    val roomId: String,
    var roomType: Int,
    val host: String
) {
    val players = arrayOf("", "")
    var started = false
    var spells: Array<Spell>? = null
    var startMs: Long = 0
    var gameTime: Int = 0 // 比赛时长，分
    var countDown: Int = 0 // 倒计时，秒
    var spellStatus: Array<SpellStatus>? = null // 每个格子的状态
    val score = intArrayOf(0, 0) // 比分
    var games: Array<String> = arrayOf("6", "7", "8", "10", "11", "12", "13", "14", "15", "16", "17", "18")
    var ranks: Array<String>? = null
    var isPrivate: Boolean = false
    var locked = false // 连续多局就需要锁上
    var needWin: Int = 0 // 需要赢几局才算赢
    var cdTime = 30
    val changeCardCount = intArrayOf(0, 0)
    val lastGetTime = longArrayOf(0, 0) // 上次收卡时间
    var totalPauseMs: Long = 0 // 累计暂停时长，毫秒
    var pauseBeginMs: Long = 0 // 开始暂停时刻，毫秒，0表示没暂停
    var lastWinner: Int = 0 // 上一场是谁赢，1或2
    var bpData: BpData? = null
    var linkData: LinkData? = null
    var phase: Int = 0 // 纯客户端用，服务器只记录
    val watchers = ArrayList<String>() // 观众
    var difficulty: Int = 0
    var lastOperateMs: Long = 0 // 最后一次操作的时间戳，毫秒
    var banPick: BanPick? = null
    val type
        get() = when (roomType) {
            1 -> RoomTypeNormal
            2 -> RoomTypeBP
            3 -> RoomTypeLink
            else -> throw HandlerException("不支持的游戏类型")
        }

    fun isHost(token: String) =
        if (host.isEmpty()) token in players
        else token == host
}
