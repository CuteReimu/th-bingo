package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class ResetRoomCs : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (room.started) throw HandlerException("游戏已开始，不能重置房间")
        for (i in room.score.indices)
            room.score[i] = 0
        room.locked = false
        for (i in room.changeCardCount.indices) {
            room.changeCardCount[i] = 0
            room.lastGetTime[i] = 0
        }
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}