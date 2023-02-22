package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

data class SetPhaseCs(val phase: Int) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host.isNotEmpty() && room.host != token) throw HandlerException("你不是房主")
        room.phase = phase
        Store.putRoom(room)
        Store.notifyPlayersInRoom(token, protoName, Message(SetPhaseSc(phase)))
    }
}
