package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store
import java.util.*

class GetSpellsCs : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.started) throw HandlerException("游戏还未开始")
        val now = System.currentTimeMillis()
        ctx.writeMessage(
            Message(
                reply = protoName,
                trigger = player.name,
                data = SpellListSc(
                    spells = room.spells!!,
                    time = System.currentTimeMillis(),
                    startTime = room.startMs,
                    whoseTurn = room.bpData?.whoseTurn ?: 0,
                    banPick = room.bpData?.banPick ?: 0,
                    totalPauseTime = room.totalPauseMs,
                    pauseBeginMs = room.pauseBeginMs,
                    pauseEndMs = room.pauseEndMs,
                    status = IntArray(room.spellStatus!!.size) { i -> room.spellStatus!![i].value },
                    linkData = room.linkData,
                    phase = room.phase,
                    lastGetTime = room.lastGetTime.map {
                        if (room.pauseBeginMs > 0) it + (now - room.pauseBeginMs) else it
                    }.toLongArray()
                )
            )
        )
    }
}