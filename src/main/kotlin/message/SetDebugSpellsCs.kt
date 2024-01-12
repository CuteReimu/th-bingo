package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*
import java.util.*

class SetDebugSpellsCs(val spells: IntArray?) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (spells != null && spells.size != 25) throw HandlerException("参数错误")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        room.debugSpells = spells
        Store.putRoom(room)
        ctx.writeMessage(Message(name = "set_debug_spells_sc", reply = protoName))
    }
}
