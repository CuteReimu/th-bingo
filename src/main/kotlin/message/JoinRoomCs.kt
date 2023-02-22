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
        if (player.roomId != null) throw HandlerException("已经在房间里了")
        val room = Store.getRoom(rid) ?: throw HandlerException("房间不存在")
        val host = Store.getPlayer(room.host) ?: throw HandlerException("找不到房主")
        if (host.name == name) throw HandlerException("名字重复")
        var ok = false
        for (i in room.players.indices) {
            if (ok) {
                if (room.players[i].isNotEmpty()) {
                    val player2 = Store.getPlayer(room.players[i]) ?: throw HandlerException("找不到玩家")
                    if (player2.name == name) throw HandlerException("名字重复")
                }
            } else if (room.players[i].isEmpty()) {
                ok = true
                room.players[i] = token
            }
        }
        if (!ok) throw HandlerException("房间满了")
        Store.putPlayer(Player(token = token, roomId = rid, name = name))
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
