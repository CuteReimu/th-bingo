package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.apache.log4j.Logger
import org.tfcc.bingo.*
import java.util.*

class StartGameCs : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (room.started) throw HandlerException("游戏已经开始")
        if (room.players.contains("")) throw HandlerException("玩家没满")
        val start = System.currentTimeMillis()
        room.spells = room.type.randSpells(
            room.games, room.ranks,
            when (room.difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
        val now = System.currentTimeMillis()
        logger.debug("随符卡耗时：${now - start}")
        SpellLog.logRandSpells(room.spells!!, room.type)
        room.started = true
        room.startMs = now
        room.spellStatus = Array(room.spells!!.size) { SpellStatus.NONE }
        room.locked = true
        room.banPick = null
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
                    whoseTurn = room.bpData?.whoseTurn ?: 0,
                    banPick = room.bpData?.banPick ?: 0,
                    linkData = room.linkData,
                    phase = room.phase,
                    pauseBeginMs = 0L,
                    pauseEndMs = 0L,
                    status = null,
                    totalPauseTime = 0L,
                    lastGetTime = room.lastGetTime
                )
            )
        )
        if (!room.isPrivate && !room.players.contains(Store.robotPlayer.token)) // 单人练习模式不推送
            MiraiPusher.push(room)
    }

    companion object {
        private val logger: Logger = Logger.getLogger(StartGameCs::class.java)
    }
}
