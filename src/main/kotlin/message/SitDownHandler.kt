package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push

object SitDownHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("找不到房间")
        !room.started || throw HandlerException("游戏已开始")
        !room.locked || throw HandlerException("连续比赛还未结束")
        val index = room.players.indexOfFirst { it == null }
        index >= 0 || throw HandlerException("房间已满")
        room.watchers.remove(player) || throw HandlerException("你不是观众")
        room.players[index] = player
        room.push(
            "push_sit_down", JsonObject(
                mapOf(
                    "name" to JsonPrimitive(player.name),
                    "position" to JsonPrimitive(index),
                )
            )
        )
        return null
    }
}
