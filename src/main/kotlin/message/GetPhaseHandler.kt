package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler

object GetPhaseHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement {
        val room = player.room ?: throw HandlerException("不在房间里")
        return JsonObject(mapOf("phase" to JsonPrimitive(room.phase)))
    }
}
