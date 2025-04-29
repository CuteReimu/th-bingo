package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.BpData
import org.tfcc.bingo.message.HandlerException
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

object RoomTypeBP : RoomType {
    override val name = "BP赛"

    override val canPause = true

    override fun onStart(room: Room) {
        room.bpData = BpData(
            whoseTurn = if (room.lastWinner > 0) 2 - room.lastWinner else Random.nextInt(2),
            banPick = 1,
        )
        if (room.roomConfig.blindSetting == 1) return

        if (room.roomConfig.blindSetting == 2) {
            room.spellStatus = Array(room.spells!!.size) { BOTH_HIDDEN }
            val outerRingIndex = arrayOf(0, 1, 2, 3, 4, 5, 9, 10, 14, 15, 19, 20, 21, 22, 23, 24)
            val innerRingIndex = arrayOf(6, 7, 8, 11, 13, 16, 17, 18)
            val rand = ThreadLocalRandom.current().asKotlinRandom()
            outerRingIndex.shuffle(rand)
            innerRingIndex.shuffle(rand)
            // 外环 (4, 4, 2, 6)
            for (i in 0 until 4) {
                room.spellStatus!![outerRingIndex[i]] = LEFT_SEE_ONLY
            }
            for (i in 4 until 8) {
                room.spellStatus!![outerRingIndex[i]] = RIGHT_SEE_ONLY
            }
            for (i in 8 until 10) {
                room.spellStatus!![outerRingIndex[i]] = NONE
            }
            // 内环 (1, 1, 2, 4)
            for (i in 0 until 1) {
                room.spellStatus!![innerRingIndex[i]] = LEFT_SEE_ONLY
            }
            for (i in 1 until 2) {
                room.spellStatus!![innerRingIndex[i]] = RIGHT_SEE_ONLY
            }
            for (i in 2 until 4) {
                room.spellStatus!![innerRingIndex[i]] = NONE
            }
        } else if (room.roomConfig.blindSetting == 3) {
            room.spellStatus = Array(room.spells!!.size) { ONLY_REVEAL_STAR }
        }
    }

    @Throws(HandlerException::class)
    override fun randSpells(spellCardVersion: Int, games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell> {
        if (difficulty == 4) {
            return SpellFactory.randSpellsBPOD(spellCardVersion, games, ranks, 3)
        }
        return SpellFactory.randSpellsBP(spellCardVersion, games, ranks, when (difficulty) {
            1, 2 -> 10
            else -> 5
        })
    }

    override fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int) {
        throw HandlerException("不支持选卡操作")
    }

    override fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean) {
        isHost || throw HandlerException("权限不足")
        room.bpData!!.banPick == 2 || throw HandlerException("还没轮到收卡的时候")
        when (val st = room.spellStatus!![spellIndex]) {
            LEFT_SELECT -> {
                if (success) {
                    room.spellStatus!![spellIndex] = LEFT_GET
                } else {
                    room.bpData!!.spellFailedCountA[spellIndex]++
                    room.spellStatus!![spellIndex] = NONE
                }
            }

            RIGHT_SELECT -> {
                if (success) {
                    room.spellStatus!![spellIndex] = RIGHT_GET
                } else {
                    room.bpData!!.spellFailedCountB[spellIndex]++
                    room.spellStatus!![spellIndex] = NONE
                }
            }

            else -> throw HandlerException("符卡状态不正确：$st")
        }
    }

    override fun pushSpells(room: Room, spellIndex: Int, causer: String) {
        val status = room.spellStatus!![spellIndex]
        val allStatus = JsonObject(
            mapOf(
                "index" to JsonPrimitive(spellIndex),
                "status" to JsonPrimitive(status.value),
                "causer" to JsonPrimitive(causer),
                "spell_failed_count_a" to JsonPrimitive(room.bpData!!.spellFailedCountA[spellIndex]),
                "spell_failed_count_b" to JsonPrimitive(room.bpData!!.spellFailedCountB[spellIndex]),
            )
        )
        room.host?.push("push_update_spell_status", allStatus)
        room.watchers.forEach { it.push("push_update_spell_status", allStatus) }
        for (i in room.players.indices) {
            val oldStatus = room.spellStatusInPlayerClient!![i][spellIndex]
            val newStatus = formatSpellStatus(room, status, i, spellIndex)
            if (oldStatus != newStatus) {
                room.players[i]?.push(
                    "push_update_spell_status", JsonObject(
                        mapOf(
                            "index" to JsonPrimitive(spellIndex),
                            "status" to JsonPrimitive(status.value),
                            "causer" to JsonPrimitive(causer),
                            "spell_failed_count_a" to JsonPrimitive(room.bpData!!.spellFailedCountA[spellIndex]),
                            "spell_failed_count_b" to JsonPrimitive(room.bpData!!.spellFailedCountB[spellIndex]),
                        )
                    )
                )
                room.spellStatusInPlayerClient!![i][spellIndex] = newStatus
            }
        }
    }

    override fun getAllSpellStatus(room: Room, playerIndex: Int): List<Int> {
        if (playerIndex == -1)
            return super.getAllSpellStatus(room, playerIndex)
        return if (room.roomConfig.blindSetting == 1)
            super.getAllSpellStatus(room, playerIndex)
        else room.spellStatus!!.mapIndexed { index, status -> formatSpellStatus(room, status, playerIndex, index) }
    }

    private fun formatSpellStatus(room: Room, status: SpellStatus, playerIndex: Int, spellIndex: Int): Int {
        var st = status
        // 如果是不对称的可见情况，将当前能看到的改为NONE，否则为HIDDEN
        if (st == LEFT_SEE_ONLY) {
            st = if (playerIndex == 0) NONE else BOTH_HIDDEN
        } else if (st == RIGHT_SEE_ONLY) {
            st = if (playerIndex == 1) NONE else BOTH_HIDDEN
        }
        return st.value
    }

    @Throws(HandlerException::class)
    override fun handleNextRound(room: Room) {
        if (room.bpData?.banPick != 2) {
            throw HandlerException("现在不是这个操作的时候")
        }
        nextRound(room)
    }

    fun nextRound(room: Room) {
        val bp = room.bpData!!
        when (++bp.round) {
            1 -> bp.whoseTurn = 1 - bp.whoseTurn
            2 -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 0
            }

            3 -> bp.whoseTurn = 1 - bp.whoseTurn
            4 -> {}
            5 -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 2
            }

            6 -> bp.banPick = 1
            7 -> bp.whoseTurn = 1 - bp.whoseTurn
            8 -> {}
            9 -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 0
            }

            10 -> {}
            11 -> bp.whoseTurn = 1 - bp.whoseTurn
            12 -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 2
            }

            13 -> bp.banPick = 1
            14 -> {}
            15 -> bp.whoseTurn = 1 - bp.whoseTurn
            16 -> bp.banPick = 0
            else -> {
                if (!bp.lessThan4 && bp.round % 5 == 1) {
                    var count = 0
                    for (status in room.spellStatus!!) {
                        if (status == NONE)
                            count++
                    }
                    if (count < 4)
                        bp.lessThan4 = true
                }
                if (bp.lessThan4) {
                    if (bp.banPick == 2) {
                        bp.whoseTurn = 1 - bp.whoseTurn
                        bp.banPick = 0
                    } else {
                        bp.banPick = 2
                    }
                } else {
                    when (bp.round % 5) {
                        0 -> {
                            bp.whoseTurn = 1 - bp.whoseTurn
                            bp.banPick = 2
                        }

                        1 -> {
                            bp.whoseTurn = 1 - bp.whoseTurn
                            bp.banPick = 0
                        }

                        3 -> bp.whoseTurn = 1 - bp.whoseTurn
                    }
                }
            }
        }
        room.push("push_bp_game_next_round", JsonObject(mapOf(
            "whose_turn" to JsonPrimitive(room.bpData!!.whoseTurn),
            "ban_pick" to JsonPrimitive(room.bpData!!.banPick),
        )))
    }
}
