package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*

object FinishSpellHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val idx = m["index"]!!.jsonPrimitive.int
        idx in 0..24 || throw HandlerException("idx超出范围")
        val success = m["success"]?.jsonPrimitive?.booleanOrNull ?: true
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还没开始")
        var playerIndex = room.players.indexOf(player)
        val isHost = room.isHost(player)
        playerIndex >= 0 || isHost || throw HandlerException("没有权限")
        if (room.type is RoomTypeLink && player === room.host)
            playerIndex = m["player_index"]!!.jsonPrimitive.int
        val page = room.dualPageData?.playerCurrentPage[playerIndex] ?: 0
        room.type.handleFinishSpell(room, isHost, playerIndex, idx, success)
        room.type.pushSpells(room, idx, player.name)
        return null
    }
}
