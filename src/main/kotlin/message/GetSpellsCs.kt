package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import java.util.*

class GetSpellsCs : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
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
                    whoseTurn = room.bpData?.whoseTurn ?: 0,
                    banPick = room.bpData?.banPick ?: 0,
                    totalPauseTime = room.totalPauseMs,
                    pauseBeginMs = room.pauseBeginMs,
                    status = IntArray(room.spellStatus!!.size) { i -> room.spellStatus!![i].value },
                    linkData = room.linkData,
                    phase = room.phase
                )
            )
        )
    }
}