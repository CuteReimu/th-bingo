package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import java.util.concurrent.ThreadLocalRandom

object RoomTypeBP : RoomType {
    override fun canPause(): Boolean {
        return false
    }

    override fun onStart(room: Room) {
        room.bpData = BpData(
            whoseTurn = if (room.lastWinner > 0) room.lastWinner - 1 else ThreadLocalRandom.current().nextInt(2),
            banPick = 1,
            round = 0U,
            lessThan4 = false
        )
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>): Array<Spell> {
        return SpellFactory.randSpells(games, ranks, 5)
    }

    @Throws(HandlerException::class)
    override fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus {
        val st = room.spellStatus!![idx]
        if (token == room.players!![0]) {
            if (room.bpData!!.whoseTurn != 0)
                throw HandlerException("不是你的回合")
            if (st != SpellStatus.NONE ||
                room.bpData!!.banPick == 0 && status != SpellStatus.LEFT_SELECT ||
                room.bpData!!.banPick == 1 && status != SpellStatus.BANNED
            ) throw HandlerException("权限不足")
            nextRound(room)
        } else if (token == room.players!![1]) {
            if (room.bpData!!.whoseTurn != 1)
                throw HandlerException("不是你的回合")
            if (st != SpellStatus.NONE ||
                room.bpData!!.banPick == 0 && status != SpellStatus.RIGHT_SELECT ||
                room.bpData!!.banPick == 1 && status != SpellStatus.BANNED
            ) throw HandlerException("权限不足")
            nextRound(room)
        }
        return status
    }

    @Throws(HandlerException::class)
    override fun handleNextRound(room: Room) {
        if (room.bpData?.banPick != 2) {
            throw HandlerException("现在不是这个操作的时候")
        }
        nextRound(room)
    }

    private fun nextRound(room: Room) {
        val bp = room.bpData!!
        when (++bp.round) {
            1U -> bp.whoseTurn = 1 - bp.whoseTurn
            2U -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 0
            }

            3U -> bp.whoseTurn = 1 - bp.whoseTurn
            4U -> {}
            5U -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 2
            }

            6U -> bp.banPick = 1
            7U -> bp.whoseTurn = 1 - bp.whoseTurn
            8U -> {}
            9U -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 0
            }

            10U -> {}
            11U -> bp.whoseTurn = 1 - bp.whoseTurn
            12U -> {
                bp.whoseTurn = 1 - bp.whoseTurn
                bp.banPick = 2
            }

            13U -> bp.banPick = 1
            14U -> {}
            15U -> bp.whoseTurn = 1 - bp.whoseTurn
            16U -> bp.banPick = 0
            else -> {
                if (!bp.lessThan4 && bp.round % 5U == 1U) {
                    var count = 0
                    for (status in room.spellStatus!!) {
                        if (status == SpellStatus.NONE)
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
                    when (bp.round % 5U) {
                        0U -> {
                            bp.whoseTurn = 1 - bp.whoseTurn
                            bp.banPick = 2
                        }

                        1U -> {
                            bp.whoseTurn = 1 - bp.whoseTurn
                            bp.banPick = 0
                        }

                        3U -> bp.whoseTurn = 1 - bp.whoseTurn
                    }
                }
            }
        }
    }
}