package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*
import java.util.*

class StartBanPickCs(val whoFirst: Int) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (whoFirst != 0 && whoFirst != 1) throw HandlerException("参数错误")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (room.started) throw HandlerException("游戏已经开始")
        if (room.players.contains("")) throw HandlerException("玩家没满")
        if (room.banPick != null && room.banPick!!.phase != 9999) throw HandlerException("正在BP环节")
        val bp = BanPick(whoFirst)
        room.banPick = bp
        room.locked = true
        Store.putRoom(room)
        bp.notifyAll(room, player, protoName)
    }
}
