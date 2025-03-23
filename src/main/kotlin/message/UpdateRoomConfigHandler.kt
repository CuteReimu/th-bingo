package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import org.tfcc.bingo.*

object UpdateRoomConfigHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        val m = data!!.decode<RoomConfig>()
        m.rid == room.roomId || throw HandlerException("不是你所在的房间")
        room.isHost(player) || throw HandlerException("没有权限")
        m.validate()
        room.roomConfig = m
        room.push("push_update_room_config", room.roomConfig.encode())
        return null
    }
}
