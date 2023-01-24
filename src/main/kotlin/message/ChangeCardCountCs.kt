package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store

data class ChangeCardCountCs(val counts: Array<UInt>) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (counts.size != 2 || counts[0] > 9999U || counts[1] > 9999U)
            throw HandlerException("cnt参数错误")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host != token) throw HandlerException("你不是房主")
        room.changeCardCount!![0] = counts[0]
        room.changeCardCount!![1] = counts[1]
        Store.putRoom(room)
        Store.notifyPlayerInfo(token, protoName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeCardCountCs

        if (!counts.contentEquals(other.counts)) return false

        return true
    }

    override fun hashCode(): Int {
        return counts.contentHashCode()
    }

}
