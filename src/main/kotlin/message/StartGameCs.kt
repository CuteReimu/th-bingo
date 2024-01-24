package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.kotlin.logger
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
        var retryCount = 0
        while (true) {
            try {
                room.spells = room.type.randSpells(room.games, room.ranks, room.difficulty)
                break
            } catch (e: HandlerException) {
                if (++retryCount >= 10 || e.message != "符卡数量不足") {
                    logger.error("随符卡失败", e)
                    throw e
                }
            }
        }
        val now = System.currentTimeMillis()
        logger.debug("随符卡耗时：${now - start}")
        room.debugSpells?.also { debugSpells ->
            val roomType = if (room.roomType == 2) SpellConfig.BPGame else SpellConfig.NormalGame
            room.spells!!.forEachIndexed { i, _ ->
                if (debugSpells[i] != 0) {
                    SpellConfig.getSpellById(roomType, debugSpells[i])?.also { room.spells!![i] = it }
                }
            }
        }
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
                    bpData = room.bpData,
                    phase = room.phase,
                    pauseBeginMs = 0L,
                    pauseEndMs = 0L,
                    status = null,
                    totalPauseTime = 0L,
                    lastGetTime = room.lastGetTime,
                )
            )
        )
        if (!room.isPrivate && !room.players.contains(Store.robotPlayer.token)) // 单人练习模式不推送
            MiraiPusher.push(room)
    }
}
