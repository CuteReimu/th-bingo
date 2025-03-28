package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*
import org.tfcc.bingo.SpellStatus.*

object SelectSpellHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val idx = m["index"]!!.jsonPrimitive.int
        idx in 0..24 || throw HandlerException("idx超出范围")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还没开始")
        val playerIndex = room.players.indexOf(player)
        playerIndex >= 0 || throw HandlerException("没有权限")
        room.type.handleSelectSpell(room, playerIndex, idx)
        room.type.pushSpells(room, idx, player.name)
        return null
    }
}
