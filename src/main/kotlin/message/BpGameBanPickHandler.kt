package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.RoomTypeBP
import org.tfcc.bingo.SpellStatus.BANNED
import org.tfcc.bingo.SpellStatus.LEFT_SELECT
import org.tfcc.bingo.SpellStatus.NONE
import org.tfcc.bingo.SpellStatus.RIGHT_SELECT

object BpGameBanPickHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val idx = m["idx"]!!.jsonPrimitive.int
        idx in 0..24 || throw HandlerException("idx超出范围")
        val room = player.room ?: throw HandlerException("不在房间里")
        val playerIndex = room.players.indexOf(player)
        playerIndex >= 0 || throw HandlerException("你不是玩家")
        room.bpData!!.whoseTurn == playerIndex || throw HandlerException("不是你的回合")
        room.spellStatus!![idx] == NONE || throw HandlerException("这个符卡已经被选择了")
        when (room.bpData!!.banPick) {
            0 -> { // 选
                room.spellStatus!![idx] = if (playerIndex == 0) LEFT_SELECT else RIGHT_SELECT
            }

            1 -> { // ban
                room.spellStatus!![idx] = BANNED
            }

            else -> throw HandlerException("不是BP阶段")
        }
        room.type.pushSpells(room, idx, player.name)
        (room.type as RoomTypeBP).nextRound(room)
        return null
    }
}
