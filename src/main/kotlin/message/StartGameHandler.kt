package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.*
import java.util.*

object StartGameHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        !room.started || throw HandlerException("游戏已经开始")
        room.players.all { it != null } || throw HandlerException("玩家没满")
        val start = System.currentTimeMillis()
        var retryCount = 0
        while (true) {
            try {
                room.spells = room.type.randSpells(room.roomConfig.games, room.roomConfig.ranks, room.roomConfig.difficulty)
                break
            } catch (e: HandlerException) {
                if (++retryCount >= 10 || e.message != "符卡数量不足") {
                    logger.error("随符卡失败", e)
                    throw e
                }
            }
        }
        val now = System.currentTimeMillis()
        logger.debug("随符卡耗时：${now - start}")
        val debugSpells = room.debugSpells
        if (debugSpells != null) { // 测试用强制选符卡
            val roomType = SpellConfig.NORMAL_GAME
            room.spells!!.forEachIndexed { i, _ ->
                if (debugSpells[i] != 0) {
                    SpellConfig.getSpellById(roomType, debugSpells[i])?.also { room.spells!![i] = it }
                }
            }
        }
        SpellLog.logRandSpells(room.spells!!, room.type)
        room.started = true
        room.startMs = now
        room.spellStatus = Array(room.spells!!.size) { SpellStatus.NONE }
        room.spellStatusInPlayerClient = Array(room.players.size) { room.spellStatus!!.map { it.value }.toIntArray() }
        room.locked = true
        room.banPick = null
        room.type.onStart(room)
        room.push("push_start_game", room.roomConfig.encode())
        return null
    }
}
