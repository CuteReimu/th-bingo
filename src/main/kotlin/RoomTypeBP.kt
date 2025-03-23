package org.tfcc.bingo
//
// import org.tfcc.bingo.message.HandlerException
// import java.util.concurrent.ThreadLocalRandom
//
// object RoomTypeBP : RoomType {
//    override val name = "BP赛"
//
//    override val canPause = true
//
//    override fun onStart(room: Room) {
//        room.bpData = BpData(
//            whoseTurn = if (room.lastWinner > 0) room.lastWinner - 1 else ThreadLocalRandom.current().nextInt(2),
//            banPick = 1,
//            round = 0,
//            lessThan4 = false,
//            spellFailedCountA = IntArray(25),
//            spellFailedCountB = IntArray(25),
//        )
//    }
//
//    @Throws(HandlerException::class)
//    override fun randSpells(games: Array<String>?, ranks: Array<String>?, difficulty: Int?): Array<Spell> {
//        return SpellFactory.randSpellsBP(games, ranks, 5)
//    }
//
//    @Throws(HandlerException::class)
//    override fun handleUpdateSpell(
//        room: Room,
//        token: String,
//        idx: Int,
//        status: SpellStatus,
//        now: Long,
//        isReset: Boolean
//    ): SpellStatus {
//        val st = room.spellStatus!![idx]
// //        SpellLog.logSpellOperate(status, room.spells!![idx], token)
//        when (token) {
//            room.players[0] -> {
//                if (room.bpData!!.whoseTurn != 0)
//                    throw HandlerException("不是你的回合")
//                if (st != SpellStatus.NONE ||
//                    room.bpData!!.banPick == 0 && status != SpellStatus.LEFT_SELECT ||
//                    room.bpData!!.banPick == 1 && status != SpellStatus.BANNED
//                ) throw HandlerException("权限不足")
//                nextRound(room)
//            }
//
//            room.players[1] -> {
//                if (room.bpData!!.whoseTurn != 1)
//                    throw HandlerException("不是你的回合")
//                if (st != SpellStatus.NONE ||
//                    room.bpData!!.banPick == 0 && status != SpellStatus.RIGHT_SELECT ||
//                    room.bpData!!.banPick == 1 && status != SpellStatus.BANNED
//                ) throw HandlerException("权限不足")
//                nextRound(room)
//            }
//
//            else -> {
//                if (!isReset && status == SpellStatus.NONE) {
//                    if (st == SpellStatus.LEFT_SELECT) room.bpData!!.spellFailedCountA[idx]++
//                    else if (st == SpellStatus.RIGHT_SELECT) room.bpData!!.spellFailedCountB[idx]++
//                }
//            }
//        }
//        return status
//    }
//
//    @Throws(HandlerException::class)
//    override fun handleNextRound(room: Room) {
//        if (room.bpData?.banPick != 2) {
//            throw HandlerException("现在不是这个操作的时候")
//        }
//        nextRound(room)
//    }
//
//    private fun nextRound(room: Room) {
//        val bp = room.bpData!!
//        when (++bp.round) {
//            1 -> bp.whoseTurn = 1 - bp.whoseTurn
//            2 -> {
//                bp.whoseTurn = 1 - bp.whoseTurn
//                bp.banPick = 0
//            }
//
//            3 -> bp.whoseTurn = 1 - bp.whoseTurn
//            4 -> {}
//            5 -> {
//                bp.whoseTurn = 1 - bp.whoseTurn
//                bp.banPick = 2
//            }
//
//            6 -> bp.banPick = 1
//            7 -> bp.whoseTurn = 1 - bp.whoseTurn
//            8 -> {}
//            9 -> {
//                bp.whoseTurn = 1 - bp.whoseTurn
//                bp.banPick = 0
//            }
//
//            10 -> {}
//            11 -> bp.whoseTurn = 1 - bp.whoseTurn
//            12 -> {
//                bp.whoseTurn = 1 - bp.whoseTurn
//                bp.banPick = 2
//            }
//
//            13 -> bp.banPick = 1
//            14 -> {}
//            15 -> bp.whoseTurn = 1 - bp.whoseTurn
//            16 -> bp.banPick = 0
//            else -> {
//                if (!bp.lessThan4 && bp.round % 5 == 1) {
//                    var count = 0
//                    for (status in room.spellStatus!!) {
//                        if (status == SpellStatus.NONE)
//                            count++
//                    }
//                    if (count < 4)
//                        bp.lessThan4 = true
//                }
//                if (bp.lessThan4) {
//                    if (bp.banPick == 2) {
//                        bp.whoseTurn = 1 - bp.whoseTurn
//                        bp.banPick = 0
//                    } else {
//                        bp.banPick = 2
//                    }
//                } else {
//                    when (bp.round % 5) {
//                        0 -> {
//                            bp.whoseTurn = 1 - bp.whoseTurn
//                            bp.banPick = 2
//                        }
//
//                        1 -> {
//                            bp.whoseTurn = 1 - bp.whoseTurn
//                            bp.banPick = 0
//                        }
//
//                        3 -> bp.whoseTurn = 1 - bp.whoseTurn
//                    }
//                }
//            }
//        }
//    }
// }
