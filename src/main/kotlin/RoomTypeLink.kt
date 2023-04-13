package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import kotlin.math.abs

object RoomTypeLink : RoomType {
    override val canPause = false

    override fun onStart(room: Room) {
        room.spellStatus!![0] = SpellStatus.LEFT_SELECT
        room.spellStatus!![4] = SpellStatus.RIGHT_SELECT
        val linkData = LinkData(ArrayList(), ArrayList(), 0, 0, 0, 0)
        linkData.linkIdxA.add(0)
        linkData.linkIdxB.add(4)
        room.linkData = linkData
    }

    @Throws(HandlerException::class)
    override fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        return SpellFactory.randSpellsLink(games, ranks, difficulty)
    }

    @Throws(HandlerException::class)
    override fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus {
        val st = room.spellStatus!![idx]
        if (status == SpellStatus.BANNED)
            throw HandlerException("不支持的操作")
//        SpellLog.logSpellOperate(status, room.spells!![idx], token)
        return when (token) {
            room.host -> status
            room.players[0] -> {
                if ((room.linkData!!.startMsA > 0 || room.linkData!!.startMsB > 0) &&
                    room.linkData!!.linkIdxA.last() == 24
                ) throw HandlerException("选卡已结束")
                when (status) {
                    SpellStatus.LEFT_SELECT -> {
                        if (room.linkData!!.linkIdxA.contains(idx))
                            throw HandlerException("已经选了这张卡")
                        val idx0 = room.linkData!!.linkIdxA.last()
                        val diff = abs(idx - idx0)
                        if (idx0 == 24 || diff != 1 && diff != 4 && diff != 5 && diff != 6)
                            throw HandlerException("不合理的选卡")
                        room.linkData!!.linkIdxA.add(idx)
                        if (st == SpellStatus.RIGHT_SELECT) SpellStatus.BOTH_SELECT else status
                    }

                    SpellStatus.NONE -> {
                        if (room.linkData!!.linkIdxA.size <= 1)
                            throw HandlerException("初始选卡不能删除")
                        if (room.linkData!!.linkIdxA.last() != idx)
                            throw HandlerException("只能删除最后一张卡")
                        room.linkData!!.linkIdxA.removeLast()
                        if (st == SpellStatus.BOTH_SELECT) SpellStatus.RIGHT_SELECT else status
                    }

                    else -> {
                        if (room.host.isNotEmpty())
                            throw HandlerException("权限不足")
                        status
                    }
                }
            }

            room.players[1] -> {
                if ((room.linkData!!.startMsA > 0 || room.linkData!!.startMsB > 0) &&
                    room.linkData!!.linkIdxB.last() == 20
                ) throw HandlerException("选卡已结束")
                when (status) {
                    SpellStatus.RIGHT_SELECT -> {
                        if (room.linkData!!.linkIdxB.contains(idx))
                            throw HandlerException("已经选了这张卡")
                        val idx0 = room.linkData!!.linkIdxB.last()
                        val diff = abs(idx - idx0)
                        if (idx0 == 20 || diff != 1 && diff != 4 && diff != 5 && diff != 6)
                            throw HandlerException("不合理的选卡")
                        room.linkData!!.linkIdxB.add(idx)
                        if (st == SpellStatus.LEFT_SELECT) SpellStatus.BOTH_SELECT else status
                    }

                    SpellStatus.NONE -> {
                        if (room.linkData!!.linkIdxB.size <= 1)
                            throw HandlerException("初始选卡不能删除")
                        if (room.linkData!!.linkIdxB.last() != idx)
                            throw HandlerException("只能删除最后一张卡")
                        room.linkData!!.linkIdxB.removeLast()
                        if (st == SpellStatus.BOTH_SELECT) SpellStatus.LEFT_SELECT else status
                    }

                    else -> {
                        if (room.host.isNotEmpty())
                            throw HandlerException("权限不足")
                        status
                    }
                }
            }

            else -> throw HandlerException("内部错误")
        }
    }
}