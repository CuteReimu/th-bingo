package org.tfcc.bingo

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.tfcc.bingo.message.HandlerException

interface RequestHandler {
    @Throws(HandlerException::class)
    fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement?
}

@Serializable
class ResponseMessage(
    val code: Int,
    val msg: String,
    val data: JsonElement?,
    val echo: JsonElement?,
)

@Serializable
class PushMessage(
    @SerialName("push_action")
    val pushAction: String,
    val data: JsonElement?,
)
