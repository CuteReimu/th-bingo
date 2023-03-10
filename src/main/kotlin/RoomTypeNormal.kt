package org.tfcc.bingo

import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.HandlerException
import java.util.*

object RoomTypeNormal : RoomType {
    override fun canPause(): Boolean {
        return true
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        return SpellFactory.randSpells(games, ranks, difficulty)
    }

    @Throws(HandlerException::class)
    override fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus {
        val st = room.spellStatus!![idx]
        if (status == BANNED)
            throw HandlerException("不支持的操作")
        val now = Date().time
        if (room.pauseBeginMs != 0L && token != room.host)
            throw HandlerException("暂停中，不能操作")
        if (room.startMs <= now - room.gameTime.toLong() * 60000L - room.totalPauseMs)
            throw HandlerException("游戏时间到")
        if (room.startMs > now - room.countDown.toLong() * 1000L && !status.isSelectStatus() && !(status == NONE && st.isSelectStatus()))
            throw HandlerException("倒计时还没结束")
        SpellLog.logSpellOperate(st, room.spells!![idx], token)
        return when (token) {
            room.host ->
                status

            room.players[0] -> {
                if (room.host.isNotEmpty()) {
                    if (status.isRightStatus() || st == LEFT_GET && status != LEFT_GET)
                        throw HandlerException("权限不足")
                    if (st == RIGHT_GET)
                        throw HandlerException("对方已打完")
                }
                when (status) {
                    LEFT_GET ->
                        status

                    LEFT_SELECT ->
                        if (st == RIGHT_SELECT) BOTH_SELECT else status

                    NONE ->
                        if (st == BOTH_SELECT) RIGHT_SELECT else status

                    else ->
                        throw HandlerException("内部错误")
                }
            }

            room.players[1] -> {
                if (room.host.isNotEmpty()) {
                    if (status.isLeftStatus() || st == RIGHT_GET && status != RIGHT_GET)
                        throw HandlerException("权限不足")
                    if (st == LEFT_GET)
                        throw HandlerException("对方已打完")
                }
                when (status) {
                    RIGHT_GET ->
                        status

                    RIGHT_SELECT ->
                        if (st == LEFT_SELECT) BOTH_SELECT else status

                    NONE ->
                        if (st == BOTH_SELECT) LEFT_SELECT else status

                    else ->
                        throw HandlerException("内部错误")
                }
            }

            else ->
                throw HandlerException("内部错误")
        }
    }
}