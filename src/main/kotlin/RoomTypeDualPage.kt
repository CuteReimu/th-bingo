package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.SpellStatus.LEFT_GET
import org.tfcc.bingo.SpellStatus.LEFT_SELECT
import org.tfcc.bingo.message.DualPageData
import org.tfcc.bingo.message.HandlerException

object RoomTypeDualPage : RoomType {
    override val name = "翻面赛"

    override val canPause = true

    override fun onStart(room: Room) {
        val dualPageSpells = SpellFactory.randSpellsDualPage(
            room.roomConfig.games,
            room.roomConfig.ranks,
            when (room.roomConfig.difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            },
            room.roomConfig.diffLevel ?: 0,
            room.spells!!.map { it.star }.toIntArray()
        )
        room.dualPageData = DualPageData(dualPageSpells)
        val transitionCount = room.roomConfig.transitionCount ?: 5
        room.spells!!.indices.shuffled().take(transitionCount).forEach {
            room.spells!![it].isTransition = true
        }
        room.dualPageData!!.spells2.indices.shuffled().take(transitionCount).forEach {
            room.dualPageData!!.spells2[it].isTransition = true
        }
    }

    override fun handleNextRound(room: Room) {
        throw HandlerException("不支持下一回合的游戏类型")
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell> {
        return SpellFactory.randSpells(
            games, ranks, when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
    }

    override fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int) {
        var st = room.spellStatus!![spellIndex]
        if (playerIndex == 1) st = st.opposite()
        room.pauseBeginMs == 0L || throw HandlerException("暂停中，不能操作")
        var now = System.currentTimeMillis()
        val gameTime = room.roomConfig.gameTime.toLong() * 60000L
        val countdown = room.roomConfig.countdown.toLong() * 1000
        if (room.startMs <= now - gameTime - countdown - room.totalPauseMs &&
            !room.isHost(room.players[playerIndex]!!) && !room.scoreDraw()
        )
            throw HandlerException("游戏时间到")
        if (now < room.startMs + countdown) {
            if (st % 10 == 1) throw HandlerException("倒计时阶段不能抢卡")
        }
        // 选卡CD
        val cdTime = room.roomConfig.cdTime ?: 0
        if (cdTime > 0) {
            val lastGetTime = room.lastGetTime[playerIndex]
            val nextCanSelectTime = lastGetTime + (cdTime - 1) * 1000L // 服务器扣一秒，以防网络延迟
            val remainSelectTime = nextCanSelectTime - now
            if (remainSelectTime > 0)
                throw HandlerException("还有${remainSelectTime / 1000 + 1}秒才能选卡")
        }

        val page = room.dualPageData!!.playerCurrentPage[playerIndex]

        room.spellStatus!![spellIndex] = when {
            st / 100 % 10 == 2 -> throw HandlerException("你已打完")
            st % 10 == 2 -> throw HandlerException("对方已打完")
            st / 100 % 10 == 0 -> st + 100 + page * 1000
            st / 100 % 10 == 1 -> throw HandlerException("重复选卡")
            else -> throw HandlerException("状态错误：$st")
        }.run { if (playerIndex == 1) opposite() else this }

        // 无导播模式不记录
        room.host != null || return
        // 等操作结束后再记录
        if (now < room.startMs + countdown) {
            // 倒计时没结束，需要按照倒计时已经结束的时间点计算开始收卡的时间
            now = room.startMs + countdown
        }
        val playerName = room.players[playerIndex]!!.name
        var status = LEFT_SELECT
        if (playerIndex == 1) status = status.opposite()
        val spells = if (page == 0) room.spells!! else room.dualPageData!!.spells2
        SpellLog.logSpellOperate(status, spells[spellIndex], playerName, now, SpellLog.GameType.NORMAL)
    }

    override fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean) {
        success || throw HandlerException("标准赛不支持收卡失败的操作")
        playerIndex >= 0 || throw HandlerException("只有玩家才能主动收卡")
        var st = room.spellStatus!![spellIndex]
        if (playerIndex == 1) st = st.opposite()
        room.pauseBeginMs == 0L || throw HandlerException("暂停中，不能操作")
        var now = System.currentTimeMillis()
        val gameTime = room.roomConfig.gameTime.toLong() * 60000L
        val countdown = room.roomConfig.countdown.toLong() * 1000
        if (room.startMs <= now - gameTime - countdown - room.totalPauseMs &&
            !room.isHost(room.players[playerIndex]!!) && !room.scoreDraw()
        )
            throw HandlerException("游戏时间到")
        if (now < room.startMs + countdown) {
            throw HandlerException("倒计时还没结束")
        }

        val page = room.dualPageData!!.playerCurrentPage[playerIndex]

        room.spellStatus!![spellIndex] = when {
            st / 100 % 10 == 2 -> throw HandlerException("你已打完")
            st % 10 == 2 -> throw HandlerException("对方已打完")
            st / 100 % 10 != 1 -> throw HandlerException("你还未选卡")
            else -> 200 + page * 1000
        }.run { if (playerIndex == 1) opposite() else this }

        room.lastGetTime[playerIndex] = now // 更新上次收卡时间

        val spells = if (page == 0) room.spells!! else room.dualPageData!!.spells2
        if (spells[spellIndex].isTransition) {
            room.dualPageData!!.playerCurrentPage[playerIndex] = 1 - page
            room.push("push_switch_page", JsonObject(mapOf(
                "player_index" to JsonPrimitive(playerIndex),
                "page" to JsonPrimitive(1 - page),
            )))
        }

        // 无导播模式不记录
        room.host != null || return
        // 等操作结束后再记录
        if (now < room.startMs + countdown) {
            // 倒计时没结束，需要按照倒计时已经结束的时间点计算开始收卡的时间
            now = room.startMs + countdown
        }
        val playerName = room.players[playerIndex]!!.name
        var status = LEFT_GET
        if (playerIndex == 1) status = status.opposite()
        SpellLog.logSpellOperate(status, spells[spellIndex], playerName, now, SpellLog.GameType.NORMAL)
    }

    /**
     * 收了一定数量的卡之后，隐藏对方的选卡
     */
    private fun formatSpellStatus(room: Room, status: Int, playerIndex: Int): Int {
        var st = status
        if (st.isSelectStatus()) {
            if ((room.roomConfig.reservedType ?: 0) == 0) {
                // 个人赛对方收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0 && room.spellStatus!!.count { it % 10 == 2 } >= 5) {
                    if (status % 10 == 1) st = st / 100 * 100
                } else if (playerIndex == 1 && room.spellStatus!!.count { it / 100 % 10 == 2 } >= 5) {
                    if (status / 100 % 10 == 1) st = st % 100
                }
            } else if (room.spellStatus!!.count { it % 10 == 2 || it / 100 % 10 == 2 } >= 5) {
                // 团体赛双方合计收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0) {
                    if (status % 10 == 1) st = st / 100 * 100
                } else if (playerIndex == 1) {
                    if (status / 100 % 10 == 1) st = st % 100
                }
            }
        }
        return st
    }

    override fun pushSpells(room: Room, spellIndex: Int, causer: String) {
        val status = room.spellStatus!![spellIndex]
        val allStatus = JsonObject(
            mapOf(
                "index" to JsonPrimitive(spellIndex),
                "status" to JsonPrimitive(status),
                "causer" to JsonPrimitive(causer),
            )
        )
        room.host?.push("push_update_spell_status", allStatus)
        for (i in room.players.indices) {
            val oldStatus = room.spellStatusInPlayerClient!![i][spellIndex]
            val newStatus = formatSpellStatus(room, status, i)
            if (oldStatus != newStatus) {
                room.players[i]?.push(
                    "push_update_spell_status", JsonObject(
                        mapOf(
                            "index" to JsonPrimitive(spellIndex),
                            "status" to JsonPrimitive(newStatus),
                            "causer" to JsonPrimitive(causer),
                        )
                    )
                )
                room.spellStatusInPlayerClient!![i][spellIndex] = newStatus
            }
        }
        room.watchers.forEach { it.push("push_update_spell_status", allStatus) }
    }

    override fun getAllSpellStatus(room: Room, playerIndex: Int): List<Int> {
        if (playerIndex == -1)
            return super.getAllSpellStatus(room, playerIndex)
        return room.spellStatus!!.map { formatSpellStatus(room, it, playerIndex) }
    }

    /** 是否平局 */
    private fun Room.scoreDraw(): Boolean {
        var left = 0
        spellStatus!!.forEach {
            if (it / 100 % 10 == 2) left++
            else if (it % 10 == 2) left--
        }
        return left == 0
    }
}
