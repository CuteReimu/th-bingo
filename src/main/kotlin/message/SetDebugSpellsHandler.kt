package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler

object SetDebugSpellsHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val spells = m["spells"]?.jsonArray
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        if (spells == null) {
            room.debugSpells = null
        } else if (spells.size == 25) {
            room.debugSpells = IntArray(25) { spells[it].jsonPrimitive.int }
        } else {
            throw HandlerException("符卡数量不正确")
        }
        return null
    }
}
