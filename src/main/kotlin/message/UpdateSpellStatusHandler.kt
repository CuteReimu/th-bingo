package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.*
import org.tfcc.bingo.SpellStatus.*

object UpdateSpellStatusHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val idx = m["index"]!!.jsonPrimitive.int
        val status = m["status"]!!.jsonPrimitive.int
        idx in 0..24 || throw HandlerException("idx超出范围")
        val spellStatus = status.toSpellStatus()
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还没开始")
        if (room.host != null) { // 自己是房主则有权限
            room.host === player || throw HandlerException("没有权限")
        } else { // 无房主模式，自己是选手并且另一个选手是机器人才有权限
            val oldStatus = room.spellStatus!![idx]
            player in room.players && room.players.any { it!!.name == Store.ROBOT_NAME } || run {
                // 或者自己可以取消选卡
                val index = room.players.indexOf(player)
                when (index) {
                    0 -> oldStatus == LEFT_SELECT && spellStatus == NONE ||
                        oldStatus == BOTH_SELECT && spellStatus == RIGHT_SELECT

                    1 -> oldStatus == RIGHT_SELECT && spellStatus == NONE ||
                        oldStatus == BOTH_SELECT && spellStatus == LEFT_SELECT

                    else -> false
                }
            } || throw HandlerException("没有权限")
        }
        if (room.host === player && room.linkData != null) {
            if (!room.linkData!!.selectCompleteA() || room.linkData!!.selectCompleteB()) {
                throw HandlerException("link赛符卡还未选完，暂不能操作")
            }
        }
        room.spellStatus!![idx] = spellStatus
        room.type.pushSpells(room, idx, player.name)
        return null
    }
}
