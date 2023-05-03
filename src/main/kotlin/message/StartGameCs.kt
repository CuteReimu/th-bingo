package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*
import java.util.*

class StartGameCs(
    val gameTime: Int, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int, // 倒计时，单位：秒
    val games: Array<String>,
    val ranks: Array<String>?,
    val needWin: Int,
    val difficulty: Int,
    val enableTools: Boolean?,
    val isPrivate: Boolean?
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
        val start = System.currentTimeMillis()
        room.spells = room.type.randSpells(
            games, ranks,
            when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
        val now = System.currentTimeMillis()
        println("随符卡耗时：${now - start}")
        SpellLog.logRandSpells(room.spells!!,room.type)
        room.started = true
        room.startMs = now
        room.countDown = countdown
        room.gameTime = gameTime
        room.spellStatus = Array(room.spells!!.size) { SpellStatus.NONE }
        room.needWin = needWin.coerceAtLeast(1)
        room.locked = true
        room.difficulty = difficulty
        room.enableTools = enableTools == true
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
                    needWin = room.needWin,
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
        if (isPrivate != true && !room.players.contains(Store.robotPlayer.token)) // 单人练习模式不推送
            MiraiPusher.push(room)
    }
}
