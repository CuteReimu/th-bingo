package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*

object StopGameHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val winner = m["winner"]!!.jsonPrimitive.int
        if (winner != -1 && winner != 0 && winner != 1)
            throw HandlerException("winner不正确")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        room.started || throw HandlerException("游戏还没开始")
        if (winner >= 0) {
            room.score[winner]++
            if (room.score[winner] >= (room.roomConfig.needWin ?: 1))
                room.locked = false
            room.lastWinner = winner + 1
        }
        room.started = false
        room.spells = null
        room.lastGetTime.indices.forEach { room.lastGetTime[it] = 0 }
        room.startMs = 0
        room.spellStatus = null
        room.totalPauseMs = 0
        room.pauseBeginMs = 0
        room.pauseEndMs = 0
        room.bpData = null
        room.linkData = null
        room.push("push_stop_game", JsonObject(mapOf("winner" to JsonPrimitive(winner))))
        SpellLog.saveFile()
        return null
    }
}
