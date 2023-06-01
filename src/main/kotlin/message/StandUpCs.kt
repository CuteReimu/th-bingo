package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class StandUpCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.started) throw HandlerException("游戏已开始")
        if (room.locked) throw HandlerException("连续比赛还未结束")
        val index = room.players.indexOf(token)
        if (index < 0) throw HandlerException("你不是选手")
        if (room.host.isEmpty() && room.players[1 - index].let { it.isEmpty() || it == Store.robotPlayer.token })
            throw HandlerException("你是房间里的最后一位选手，不能成为观众")
        room.players[index] = ""
        room.watchers.add(token)
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
