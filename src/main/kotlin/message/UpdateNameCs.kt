package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class UpdateNameCs(val name: String) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (name.isEmpty()) throw HandlerException("名字为空")
        if (name == Store.robotPlayer.name) throw HandlerException("不能使用这个名字")
        if (name.toByteArray().size > 48) throw HandlerException("名字太长")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        Store.putPlayer(player.copy(name = name))
        Store.notifyPlayerInfo(token, protoName)
    }
}
