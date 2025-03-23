package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push
import java.util.*

object PauseHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val pause = m["pause"]!!.jsonPrimitive.boolean
        val room = player.room ?: throw HandlerException("不在房间里")
        room.type.canPause || throw HandlerException("不支持暂停的游戏类型")
        room.isHost(player) || throw HandlerException("没有权限")
        room.started || throw HandlerException("游戏还没开始，不能暂停")
        val now = System.currentTimeMillis()
        val gameTime = room.roomConfig.gameTime.toLong() * 60000L
        val countdown = room.roomConfig.countdown.toLong() * 1000L
        if (pause && room.startMs + gameTime + countdown + room.totalPauseMs <= now)
            throw HandlerException("游戏时间到，不能暂停")
        if (room.startMs > now - countdown)
            throw HandlerException("倒计时还没结束，不能暂停")
        if (pause) {
            if (room.pauseBeginMs == 0L)
                room.pauseBeginMs = now
        } else {
            if (room.pauseBeginMs != 0L) {
                val delta = now - room.pauseBeginMs
                room.totalPauseMs += delta
                room.pauseBeginMs = 0
                room.pauseEndMs = now
                // 暂停结束后，要把所有人的上次收卡时间往后延暂停持续时间
                for (i in room.lastGetTime.indices)
                    room.lastGetTime[i] += delta
            }
        }
        room.push("push_pause", JsonObject(mapOf("pause" to JsonPrimitive(pause))))
        return null
    }
}
