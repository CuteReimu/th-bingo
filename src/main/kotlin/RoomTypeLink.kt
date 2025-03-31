package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.HandlerException
import org.tfcc.bingo.message.LinkData
import kotlin.math.abs

object RoomTypeLink : RoomType {
    override val name = "Link赛"

    override val canPause = false

    override fun onStart(room: Room) {
        room.spellStatus!![0] = LEFT_SELECT
        room.spellStatus!![4] = RIGHT_SELECT
        val linkData = LinkData()
        linkData.linkIdxA.add(0)
        linkData.linkIdxB.add(4)
        room.linkData = linkData
    }

    override fun handleNextRound(room: Room) {
        throw HandlerException("不支持下一回合的游戏类型")
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell> {
        return SpellFactory.randSpellsLink(
            games, ranks, when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
    }

    override fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int) {
        playerIndex >= 0 || throw HandlerException("不是玩家，不能操作")
        val st = room.spellStatus!![spellIndex]
        val status =
            if (playerIndex == 0) LEFT_SELECT
            else RIGHT_SELECT

        if (playerIndex == 0) {
            if (room.linkData!!.selectCompleteA())
                throw HandlerException("你的选卡已结束")

            when (status) {
                LEFT_SELECT -> {
                    if (spellIndex in room.linkData!!.linkIdxA)
                        throw HandlerException("已经选了这张卡")
                    val idx0 = room.linkData!!.linkIdxA.last()
                    if (idx0 == 24 || !(spellIndex near idx0))
                        throw HandlerException("不合理的选卡")
                    room.linkData!!.linkIdxA.add(spellIndex)
                    room.spellStatus!![spellIndex] =
                        if (st == RIGHT_SELECT) BOTH_SELECT
                        else status
                }

                else -> throw HandlerException("还未轮到你")
            }
        } else {
            if (room.linkData!!.selectCompleteB()) {
                throw HandlerException("你的选卡已结束")
            }
            when (status) {
                RIGHT_SELECT -> {
                    if (room.linkData!!.linkIdxB.contains(spellIndex))
                        throw HandlerException("已经选了这张卡")
                    val idx0 = room.linkData!!.linkIdxB.last()
                    if (idx0 == 20 || !(spellIndex near idx0))
                        throw HandlerException("不合理的选卡")
                    room.linkData!!.linkIdxB.add(spellIndex)
                    room.spellStatus!![spellIndex] =
                        if (st == LEFT_SELECT) BOTH_SELECT
                        else status
                }

                else -> throw HandlerException("还未轮到你")
            }
        }

        val playerName = room.players[playerIndex]!!.name
        if (room.host != null && playerName != Store.ROBOT_NAME) {
            SpellLog.logSpellOperate(status, room.spells!![spellIndex], playerName, gameType = SpellLog.GameType.LINK)
        }
    }

    override fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean) {
        isHost || throw HandlerException("权限不足")
        playerIndex >= 0 || throw HandlerException("不是玩家，不能操作")
        val st = room.spellStatus!![spellIndex]
        val status =
            if (playerIndex == 0) LEFT_GET
            else RIGHT_GET

        if (playerIndex == 0) {
            if (!room.linkData!!.selectCompleteA()) {
                throw HandlerException("选卡还未结束")
            }
            room.spellStatus!![spellIndex] = if (st == RIGHT_GET) BOTH_GET else status
        } else {
            if (!room.linkData!!.selectCompleteB()) {
                throw HandlerException("选卡还未结束")
            }
            room.spellStatus!![spellIndex] = if (st == LEFT_GET) BOTH_GET else status
        }

        val playerName = room.players[playerIndex]!!.name
        if (room.host != null && playerName != Store.ROBOT_NAME) {
            SpellLog.logSpellOperate(status, room.spells!![spellIndex], playerName, gameType = SpellLog.GameType.LINK)
        }
    }

    override fun pushSpells(room: Room, spellIndex: Int, causer: String) {
        val status = room.spellStatus!![spellIndex]
        room.push(
            "push_update_spell_status", JsonObject(
                mapOf(
                    "index" to JsonPrimitive(spellIndex),
                    "status" to JsonPrimitive(status.value),
                    "causer" to JsonPrimitive(causer),
                )
            )
        )
    }

    private infix fun Int.near(other: Int): Boolean {
        when (this % 5) {
            0 -> if (other == this - 1) return false
            4 -> if (other == this + 1) return false
        }
        val diff = abs(this - other)
        return diff == 1 || diff == 4 || diff == 5 || diff == 6
    }
}
