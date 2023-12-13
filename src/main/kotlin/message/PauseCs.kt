package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store
import java.util.*

class PauseCs(val pause: Boolean) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val now = System.currentTimeMillis()
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.type.canPause) throw HandlerException("不支持暂停的游戏类型")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (!room.started) throw HandlerException("游戏还没开始，不能暂停")
        if (pause && room.startMs <= now - room.gameTime.toLong() * 60000L - room.totalPauseMs)
            throw HandlerException("游戏时间到，不能暂停")
        if (room.startMs > now - room.countDown.toLong() * 1000L)
            throw HandlerException("倒计时还没结束，不能暂停")
        if (pause) {
            if (room.pauseBeginMs == 0L)
                room.pauseBeginMs = now
        } else {
            if (room.pauseBeginMs != 0L) {
                val delta = now - room.pauseBeginMs
                room.totalPauseMs += delta
                room.lastGetTime.forEachIndexed { i, v -> if (v > 0) room.lastGetTime[i] = v + delta }
                room.pauseBeginMs = 0
                room.pauseEndMs = now
            }
        }
        Store.putRoom(room)
        Store.notifyPlayersInRoom(
            token,
            protoName,
            Message(data = PauseSc(now, room.totalPauseMs, room.pauseBeginMs, room.pauseEndMs, room.lastGetTime))
        )
    }
}
