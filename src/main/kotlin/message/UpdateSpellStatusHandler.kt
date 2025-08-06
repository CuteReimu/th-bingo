package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*

object UpdateSpellStatusHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val idx = m["index"]!!.jsonPrimitive.int
        val spellStatus = m["status"]!!.jsonPrimitive.int
        idx in 0..24 || throw HandlerException("idx超出范围")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还没开始")
        if (room.host != null) { // 自己是房主则有权限
            room.host === player || throw HandlerException("没有权限")
        } else { // 无房主模式，只要是选手就有权限
            player in room.players || throw HandlerException("没有权限")
        }
        if (room.host === player && room.linkData != null) {
            if (!room.linkData!!.selectCompleteA() || room.linkData!!.selectCompleteB()) {
                throw HandlerException("link赛符卡还未选完，暂不能操作")
            }
        }
        val oldStatus = room.spellStatus!![idx]
        room.spellStatus!![idx] = spellStatus
        if (room.dualPageData != null) {
            fun checkSwitchPlayer(playerIndex: Int) {
                val page = room.dualPageData!!.playerCurrentPage[playerIndex]
                if ((if (page == 0) room.spells!! else room.dualPageData!!.spells2)[idx].isTransition) {
                    room.dualPageData!!.playerCurrentPage[playerIndex] = 1 - page
                }
                room.push("push_switch_page", JsonObject(mapOf(
                    "player_index" to JsonPrimitive(playerIndex),
                    "page" to JsonPrimitive(1 - page),
                )))
            }
            if ((oldStatus / 100 % 10 == 2) != (spellStatus / 100 % 10 != 2)) {
                checkSwitchPlayer(0)
            }
            if (oldStatus % 10 == 2 || oldStatus / 100 % 10 == 2) {
                checkSwitchPlayer(1)
            }
        }
        room.type.pushSpells(room, idx, player.name)
        return null
    }
}
