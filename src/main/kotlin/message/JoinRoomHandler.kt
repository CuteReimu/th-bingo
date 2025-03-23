package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*

object JoinRoomHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement {
        player.room == null || throw HandlerException("已经在房间里了")
        val m = data!!.jsonObject
        val rid = m["rid"]!!.jsonPrimitive.content
        val room = Store.getRoom(rid)
        room != null || throw HandlerException("房间不存在")
        player.room = room
        val index = room!!.players.indexOfFirst { it == null }
        room.push(
            "push_join_room", JsonObject(
                mapOf(
                    "name" to JsonPrimitive(player.name),
                    "position" to JsonPrimitive(index)
                )
            )
        )
        if (index >= 0)
            room.players[index] = player
        else
            room.watchers.add(player)
        return room.roomInfo.encode()
    }
}
