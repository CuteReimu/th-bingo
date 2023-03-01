package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store

data class UpdateRoomTypeCs(val type: Int) : Handler {
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (type < 1 || type > 3) throw HandlerException("不支持的游戏类型")
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
        if (room.host.isNotEmpty()) {
            if (room.host != player.token) throw HandlerException("没有权限")
        } else {
            if (!room.players.contains(player.token)) throw HandlerException("没有权限")
        }
        room.roomType = type
        Store.putRoom(room)
        Store.notifyPlayerInfo(player.token, protoName)
    }
}
