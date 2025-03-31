package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push

object ResetRoomHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        !room.started || throw HandlerException("游戏已开始，不能重置房间")
        for (i in room.score.indices)
            room.score[i] = 0
        room.locked = false
        for (i in room.changeCardCount.indices) {
            room.changeCardCount[i] = 0
            room.lastGetTime[i] = 0
        }
        room.banPick = null
        room.push("push_reset_room", null)
        return null
    }
}
