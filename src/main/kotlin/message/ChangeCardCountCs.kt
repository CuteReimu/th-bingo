package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

class ChangeCardCountCs(val cnt: IntArray) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (cnt.size != 2 || cnt[0] < 0 || cnt[0] > 9999 || cnt[1] < 0 || cnt[1] > 9999)
            throw HandlerException("cnt参数错误")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host != token && !room.players.contains(token)) throw HandlerException("没有权限")
        room.changeCardCount[0] = cnt[0]
        room.changeCardCount[1] = cnt[1]
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }
}
