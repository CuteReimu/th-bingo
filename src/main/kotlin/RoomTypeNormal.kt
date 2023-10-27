package org.tfcc.bingo

import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.HandlerException
import java.util.*

object RoomTypeNormal : RoomType {
    override val name = "标准赛"

    override val canPause = true

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        return SpellFactory.randSpells(games, ranks, difficulty)
    }

    @Throws(HandlerException::class)
    override fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus, now: Long): SpellStatus {
        val st = room.spellStatus!![idx]
        if (status == BANNED)
            throw HandlerException("不支持的操作")
        if (room.pauseBeginMs != 0L && token != room.host)
            throw HandlerException("暂停中，不能操作")
        if (room.startMs <= now - room.gameTime.toLong() * 60000L - room.totalPauseMs && !room.isHost(token) && !room.scoreDraw())
            throw HandlerException("游戏时间到")
        if (room.startMs > now - room.countDown.toLong() * 1000L) {
            if (!status.isSelectStatus() && !(status == NONE && st.isSelectStatus()))
                throw HandlerException("倒计时还没结束")
            // 倒计时没结束，需要按照倒计时已经结束的时间点计算开始收卡的时间
            SpellLog.logSpellOperate(
                status,
                room.spells!![idx],
                token,
                room.startMs + room.countDown.toLong() * 1000L,
                SpellLog.GameType.NORMAL
            )
        } else {
            SpellLog.logSpellOperate(status, room.spells!![idx], token, gameType = SpellLog.GameType.NORMAL)
        }

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

                    LEFT_SELECT -> {
                        val remainSelectTime = room.lastGetTime[0] + (room.cdTime - 1) * 1000 - now
                        if (remainSelectTime > 0)
                            throw HandlerException("还有${remainSelectTime / 1000 + 1}秒才能选卡")
                        if (st == RIGHT_SELECT) BOTH_SELECT else status
                    }

                    NONE ->
                        if (st == BOTH_SELECT) RIGHT_SELECT else status

                    else ->
                        throw HandlerException("status不合法")
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

                    RIGHT_SELECT -> {
                        val remainSelectTime = room.lastGetTime[1] + (room.cdTime - 1) * 1000 - now
                        if (remainSelectTime > 0)
                            throw HandlerException("还有${remainSelectTime / 1000 + 1}秒才能选卡")
                        if (st == LEFT_SELECT) BOTH_SELECT else status
                    }

                    NONE ->
                        if (st == BOTH_SELECT) LEFT_SELECT else status

                    else ->
                        throw HandlerException("status不合法")
                }
            }

            else ->
                throw HandlerException("内部错误")
        }
    }

    private fun Room.scoreDraw(): Boolean {
        var left = 0
        spellStatus!!.forEach {
            if (it == LEFT_GET) left++
            else if (it == RIGHT_GET) left--
        }
        return left == 0
    }
}