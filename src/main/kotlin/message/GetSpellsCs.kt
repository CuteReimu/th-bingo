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
        ctx.writeMessage(
            Message(
                protoName,
                player.name,
                SpellListSc(
                    spells = room.spells!!,
                    time = Date().time,
                    startTime = room.startMs,
                    gameTime = room.gameTime,
                    countdown = room.countDown,
                    needWin = room.needWin,
                    whoseTurn = room.bpData!!.whoseTurn,
                    banPick = room.bpData!!.banPick,
                    totalPauseTime = room.totalPauseMs,
                    pauseBeginMs = room.pauseBeginMs,
                    status = IntArray(room.spellStatus!!.size) { i -> room.spellStatus!![i].value },
                    linkData = room.linkData!!,
                    phase = room.phase
                )
            )
        )
    }
}