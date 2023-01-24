package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

data class UpdateRoomTypeCs(val type: Int) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (type < 1 || type > 3) throw HandlerException("不支持的游戏类型")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host != token) throw HandlerException("不是房主")
        room.roomType = type
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
