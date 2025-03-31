package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.Store
import org.tfcc.bingo.push

object StandUpHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("找不到房间")
        !room.started || throw HandlerException("游戏已开始")
        !room.locked || throw HandlerException("连续比赛还未结束")
        val index = room.players.indexOf(player)
        index >= 0 || throw HandlerException("你不是选手")
        if (room.host == null && room.players[1 - index].let { it == null || it.name == Store.ROBOT_NAME })
            throw HandlerException("你是房间里的最后一位选手，不能成为观众")
        room.players[index] = null
        room.watchers.add(player)
        room.push("push_stand_up", JsonObject(mapOf("name" to JsonPrimitive(player.name))))
        return null
    }
}
