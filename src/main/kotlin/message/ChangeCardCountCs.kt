package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store

data class ChangeCardCountCs(val cnt: IntArray) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (cnt.size != 2 || cnt[0] < 0 || cnt[0] > 9999 || cnt[1] < 0 || cnt[1] > 9999)
            throw HandlerException("cnt参数错误")
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
        if (room.host != player.token && !room.players.contains(player.token)) throw HandlerException("没有权限")
        room.changeCardCount[0] = cnt[0]
        room.changeCardCount[1] = cnt[1]
        Store.putRoom(room)
        Store.notifyPlayerInfo(player.token, protoName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeCardCountCs

        if (!cnt.contentEquals(other.cnt)) return false

        return true
    }

    override fun hashCode(): Int {
        return cnt.contentHashCode()
    }

}
