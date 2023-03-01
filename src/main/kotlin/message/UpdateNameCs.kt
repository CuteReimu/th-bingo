package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store

data class UpdateNameCs(val name: String) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (name.isEmpty()) throw HandlerException("名字为空")
        if (name.toByteArray().size > 48) throw HandlerException("名字太长")
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        Store.putPlayer(player.copy(name = name))
        Store.notifyPlayerInfo(player.token, protoName)
    }
}
