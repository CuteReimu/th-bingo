package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.BpData
import org.tfcc.bingo.message.HandlerException
import kotlin.random.Random

object RoomTypeBP : RoomType {
    override val name = "BP赛"

    override val canPause = true

    override fun onStart(room: Room) {
        room.bpData = BpData(
            whoseTurn = if (room.lastWinner > 0) 2 - room.lastWinner else Random.nextInt(2),
            banPick = 1,
        )
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell> {
        return SpellFactory.randSpellsBP(games, ranks, 5)
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
        room.push(
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
        with(room.spellStatusInPlayerClient!!) {
            indices.forEach {
                this[it][spellIndex] = status.value
            }
        }
    }

    override fun updateSpellStatusPostProcesser(
        room: Room,
        player: Player,
        spellIndex: Int,
        prevStatus: SpellStatus,
        status: SpellStatus
    ) {
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
