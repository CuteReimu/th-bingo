package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.Store
import org.tfcc.bingo.push

object LeaveRoomHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        if (room.host === player || player in room.players) {
            !room.started || throw HandlerException("比赛已经开始了，不能退出")
            !room.locked || throw HandlerException("连续比赛没结束，不能退出")
        }
        player.room = null
        if (room.host === player) { // 房主退出，踢出所有人，销毁房间
            for (p in room.players) {
                if (p != null && p.name != Store.ROBOT_NAME) {
                    p.pushSelfKicked()
                }
            }
            room.watchers.forEach { it.pushSelfKicked() }
            Store.removeRoom(room.roomId)
        } else {
            val index = room.players.indexOf(player)
            if (index >= 0) {
                room.players[index] = null
            } else {
                room.watchers.remove(player)
            }
            if (room.host == null && room.players.all { it == null || it.name == Store.ROBOT_NAME }) {
                // 无房主且所有玩家都退出，解散房间
                room.watchers.forEach { it.pushSelfKicked() }
                Store.removeRoom(room.roomId)
            } else {
                room.push("push_leave_room", JsonObject(mapOf("name" to JsonPrimitive(player.name))))
            }
        }
        return null
    }

    /** 推送自己被踢了 */
    private fun Player.pushSelfKicked() {
        room = null
        push("push_leave_room", JsonObject(mapOf("name" to JsonPrimitive(name))))
    }
}
