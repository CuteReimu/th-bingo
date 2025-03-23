package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*

object CreateRoomHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement {
        player.room == null || throw HandlerException("已经在房间里了")
        val m0 = data!!.jsonObject
        val solo = m0["solo"]?.jsonPrimitive?.booleanOrNull ?: false
        val addRobot = m0["add_robot"]?.jsonPrimitive?.booleanOrNull ?: false
        val m = m0["room_config"]!!.decode<RoomConfig>()
        Store.getRoom(m.rid) == null || throw HandlerException("房间已存在")
        m.validate()
        if (solo) {
            val room = Room(m.rid, null, m)
            room.players[0] = player
            Store.putRoom(room)
            player.room = room
        } else {
            val room = Room(m.rid, player, m)
            Store.putRoom(room)
            player.room = room
        }
        if (addRobot) player.room!!.players[1] = Store.newRobotPlayer()
        return JsonObject(mapOf("rid" to JsonPrimitive(m.rid)))
    }
}
