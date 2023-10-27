package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.SpellLog
import org.tfcc.bingo.Store

class StopGameCs(val winner: Int) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (winner != -1 && winner != 0 && winner != 1)
            throw HandlerException("winner不正确")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (!room.started) throw HandlerException("游戏还没开始")
        if (winner >= 0) {
            room.score[winner]++
            if (room.score[winner] >= room.needWin)
                room.locked = false
            room.lastWinner = winner + 1
        }
        room.started = false
        room.spells = null
        room.lastGetTime.run { indices.forEach { this[it] = 0 } }
        room.startMs = 0
        room.gameTime = 0
        room.countDown = 0
        room.spellStatus = null
        room.totalPauseMs = 0
        room.pauseBeginMs = 0
        room.bpData = null
        Store.putRoom(room)
        if (winner == -1)
            Store.notifyPlayerInfo(token, protoName)
        else
            Store.notifyPlayerInfo(token, protoName, winner)
        SpellLog.saveFile()
    }
}
