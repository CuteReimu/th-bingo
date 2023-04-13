package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException
import kotlin.math.abs

object RoomTypeLink : RoomType {
    override val canPause = false

    override fun onStart(room: Room) {
        room.spellStatus!![0] = SpellStatus.LEFT_SELECT
        room.spellStatus!![4] = SpellStatus.RIGHT_SELECT
        val linkData = LinkData()
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
                if (!room.linkData!!.selectCompleteA()) { // 选卡阶段
                    when (status) {
                        SpellStatus.LEFT_SELECT -> {
                            if (room.linkData!!.linkIdxA.contains(idx))
                                throw HandlerException("已经选了这张卡")
                            val idx0 = room.linkData!!.linkIdxA.last()
                            if (idx0 == 24 || !(idx near idx0))
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

                        else -> throw HandlerException("还未轮到你")
                    }
                } else { // 收卡阶段
                    if (room.host.isNotEmpty())
                        throw HandlerException("权限不足")
                    when (status) {
                        SpellStatus.LEFT_GET ->
                            if (st == SpellStatus.RIGHT_GET) SpellStatus.BOTH_GET else status

                        SpellStatus.NONE ->
                            if (st == SpellStatus.BOTH_GET) SpellStatus.RIGHT_GET else status

                        else -> throw HandlerException("权限不足")
                    }
                }
            }

            room.players[1] -> {
                if (!room.linkData!!.selectCompleteB()) { // 选卡阶段
                    when (status) {
                        SpellStatus.RIGHT_SELECT -> {
                            if (room.linkData!!.linkIdxB.contains(idx))
                                throw HandlerException("已经选了这张卡")
                            val idx0 = room.linkData!!.linkIdxB.last()
                            if (idx0 == 20 || !(idx near idx0))
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

                        else -> throw HandlerException("还未轮到你")
                    }
                } else { // 收卡阶段
                    if (room.host.isNotEmpty())
                        throw HandlerException("权限不足")
                    when (status) {
                        SpellStatus.RIGHT_GET ->
                            if (st == SpellStatus.LEFT_GET) SpellStatus.BOTH_GET else status

                        SpellStatus.NONE ->
                            if (st == SpellStatus.BOTH_GET) SpellStatus.LEFT_GET else status

                        else -> throw HandlerException("权限不足")
                    }
                }
            }

            else -> throw HandlerException("内部错误")
        }
    }

    private infix fun Int.near(other: Int): Boolean {
        val diff = abs(this - other)
        return diff == 1 || diff == 4 || diff == 5 || diff == 6
    }
}