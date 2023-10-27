package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*
import java.util.*

class WarnPlayerCs(val name: String) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        val index = room.players.indexOfFirst { it.isNotEmpty() && Store.getPlayer(it)?.name == name }
        if (index < 0) throw HandlerException("找不到目标玩家")
        val channel = Supervisor.getChannel(room.players[index]) ?: throw HandlerException("对方已离线")
        ctx.writeMessage(
            Message(
                reply = "warn_player_cs",
                trigger = player.name,
                data = WarnPlayerSc(player.name!!, name)
            )
        )
        channel.writeMessage(
            Message(
                trigger = player.name,
                data = WarnPlayerSc(player.name, name)
            )
        )
    }
}
