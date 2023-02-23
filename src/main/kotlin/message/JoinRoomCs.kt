package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Store

data class JoinRoomCs(
    val name: String,
    val rid: String
) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (name.isEmpty()) throw HandlerException("名字为空")
        if (name.toByteArray().size > 48) throw HandlerException("名字太长")
        if (rid.isEmpty()) throw HandlerException("房间ID为空")
        if (rid.toByteArray().size > 16) throw HandlerException("房间ID太长")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId != null && Store.getRoom(player.roomId) != null) throw HandlerException("已经在房间里了")
        val room = Store.getRoom(rid) ?: throw HandlerException("房间不存在")
        if (room.host.isNotEmpty()) {
            val host = Store.getPlayer(room.host) ?: throw HandlerException("找不到房主")
            if (host.name == name) throw HandlerException("名字重复")
        }
        if (room.players.any { token1 -> Store.getPlayer(token1)?.name == name }) throw HandlerException("名字重复")
        if (room.watchers.any { token1 -> Store.getPlayer(token1)?.name == name }) throw HandlerException("名字重复")
        val index = room.players.indexOf("")
        if (index >= 0)
            room.players[index] = token
        else
            room.watchers.add(token)
        Store.putPlayer(Player(token = token, roomId = rid, name = name))
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
