package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class SitDownCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.started) throw HandlerException("游戏已开始")
        if (room.locked) throw HandlerException("连续比赛还未结束")
        val index = room.players.indexOf("")
        if (index < 0) throw HandlerException("房间已满")
        if (!room.watchers.remove(token)) throw HandlerException("你不是观众")
        room.players[index] = token
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
