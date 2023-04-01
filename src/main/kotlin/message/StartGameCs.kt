package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Difficulty
import org.tfcc.bingo.SpellLog
import org.tfcc.bingo.SpellStatus
import org.tfcc.bingo.Store
import java.util.*

class StartGameCs(
    val gameTime: Int, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int, // 倒计时，单位：秒
    val games: Array<String>,
    val ranks: Array<String>?,
    var needWin: Int,
    var difficulty: Int,
    val enableTools: Boolean?
) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (gameTime <= 0) throw HandlerException("游戏时间必须大于0")
        if (gameTime > 1440) throw HandlerException("游戏时间太长")
        if (countdown < 0) throw HandlerException("倒计时不能小于0")
        if (countdown > 86400) throw HandlerException("倒计时太长")
        if (games.size > 99) throw HandlerException("选择的作品数太多")
        if (ranks != null && ranks.size > 6) throw HandlerException("选择的难度数太多")
        if (needWin > 99) throw HandlerException("需要胜场的数值不正确")
        if (needWin <= 0) needWin = 1
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host.isNotEmpty()) {
            if (room.host != token) throw HandlerException("没有权限")
        } else {
            if (!room.players.contains(token)) throw HandlerException("没有权限")
        }
        if (room.started) throw HandlerException("游戏已经开始")
        if (room.players.contains("")) throw HandlerException("玩家没满")
        room.spells = room.type.randSpells(
            games, ranks,
            when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.N
            }
        )
        SpellLog.logRandSpells(room.spells!!)
        room.started = true
        room.startMs = Date().time
        room.countDown = countdown
        room.gameTime = gameTime
        room.spellStatus = Array(room.spells!!.size) { SpellStatus.NONE }
        room.needWin = needWin
        room.locked = true
        room.difficulty = difficulty
        room.enableTools = enableTools ?: false
        room.type.onStart(room)
        Store.putRoom(room)
        Store.notifyPlayersInRoom(
            token,
            protoName,
            Message(
                reply = null,
                trigger = player.name,
                data = SpellListSc(
                    spells = room.spells!!,
                    time = room.startMs,
                    startTime = room.startMs,
                    gameTime = gameTime,
                    countdown = countdown,
                    needWin = needWin,
                    whoseTurn = room.bpData?.whoseTurn ?: 0,
                    banPick = room.bpData?.banPick ?: 0,
                    linkData = room.linkData,
                    phase = room.phase,
                    pauseBeginMs = 0L,
                    status = null,
                    totalPauseTime = 0L,
                    difficulty = difficulty,
                    enableTools = room.enableTools
                )
            )
        )
    }
}
