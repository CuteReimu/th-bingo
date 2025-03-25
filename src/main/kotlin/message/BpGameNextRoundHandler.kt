package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push

object BpGameNextRoundHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        room.type.handleNextRound(room)
        room.push("push_bp_game_next_round", JsonObject(mapOf(
            "whose_turn" to JsonPrimitive(room.bpData!!.whoseTurn),
            "ban_pick" to JsonPrimitive(room.bpData!!.banPick),
        )))
        return null
    }
}
