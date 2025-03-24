package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler

object BanPickHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        val index = room.players.indexOf(player)
        index >= 0 || throw HandlerException("你不是玩家")
        val m = data!!.jsonObject
        val selection = m["selection"]!!.jsonPrimitive.content
        val bp = room.banPick
        if (bp == null || bp.phase == 9999) throw HandlerException("不是BP环节")
        val phase = bp.onChoose(index, selection)
        if (phase == 9999) {
            val (games, ranks) = bp.getGamesAndRanks()
            room.roomConfig.games = games
            room.roomConfig.ranks = ranks
        }
        bp.notifyAll(room)
        return null
    }
}
