package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store

class SitDownCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
        if (room.started) throw HandlerException("游戏已开始")
        if (room.locked) throw HandlerException("连续比赛还未结束")
        val index = room.players.indexOf("")
        if (index < 0) throw HandlerException("房间已满")
        if (!room.watchers.remove(player.token)) throw HandlerException("你不是观众")
        room.players[index] = player.token
        Store.putRoom(room)
        Store.notifyPlayerInfo(player.token, protoName)
    }
}
