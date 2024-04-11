package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class NextRoundCs : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        room.type.handleNextRound(room)
        Store.putRoom(room)
        Store.notifyPlayersInRoom(
            token,
            protoName,
            Message(data = NextRoundSc(room.bpData?.whoseTurn ?: 0, room.bpData?.banPick ?: 0))
        )
    }
}
