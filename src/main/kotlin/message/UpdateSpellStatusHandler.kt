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
        } else { // 无房主模式，只要是选手就有权限
            player in room.players || throw HandlerException("没有权限")
        }
        if (room.host === player && room.linkData != null) {
            if (!room.linkData!!.selectCompleteA() || room.linkData!!.selectCompleteB()) {
                throw HandlerException("link赛符卡还未选完，暂不能操作")
            }
        }
        val formerStatus = room.spellStatus!![idx]
        cardStateTransform(room, player, idx, spellStatus)
        room.type.updateSpellStatusPostProcesser(room, player, idx, formerStatus, spellStatus)
        room.type.pushSpells(room, idx, player.name)
        return null
    }

    private fun cardStateTransform(room: Room, player: Player, spellIdx: Int, stat: SpellStatus) {
        // 有导播，直接覆盖
        if (room.host != null) {
            room.spellStatus!![spellIdx] = stat
            return
        }
        // 判断玩家是左还是右
        val isLeftPlayer = (player === room.players[0])
        var st = room.spellStatus!![spellIdx]
        if (!isLeftPlayer) st = st.opposite()
        when (stat) {
            // 如果操作是置空，则只置空己方选择状态，其余状态不受影响
            NONE -> {
                room.spellStatus!![spellIdx] = when (st) {
                    LEFT_SELECT -> NONE
                    BOTH_SELECT -> RIGHT_SELECT
                    else -> stat
                }.run { if (!isLeftPlayer) opposite() else this }
            }
            // 如果操作是选择，则将对方选择状态提升为双选。由于需要保留原始结果，故不反转
            LEFT_SELECT, RIGHT_SELECT -> {
                room.spellStatus!![spellIdx] = when (st) {
                    RIGHT_SELECT -> BOTH_SELECT
                    else -> stat
                }
            }
            // 其余情况，直接覆盖即可
            else -> {
                room.spellStatus!![spellIdx] = stat
            }
        }
    }
}
