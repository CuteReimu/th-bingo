package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*
import java.util.*

class BanPickCs(val selection: String) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        val index = room.players.indexOf(token)
        if (index < 0) throw HandlerException("你不是玩家")
        val bp = room.banPick
        if (bp == null || bp.phase == 9999) throw HandlerException("不是BP环节")
        val phase = bp.onChoose(index, selection)
        if (phase == 9999) {
            val (games, ranks) = bp.getGamesAndRanks()
            room.games = games
            room.ranks = ranks
        }
        Store.putRoom(room)
        bp.notifyAll(room, player, protoName)
        if (phase == 9999) Store.notifyPlayerInfo(token, null)
    }
}
