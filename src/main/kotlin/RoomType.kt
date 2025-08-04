package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

sealed interface RoomType {
    val name: String

    fun onStart(room: Room)

    @Throws(HandlerException::class)
    fun handleNextRound(room: Room)

    val canPause: Boolean

    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell>

    @Throws(HandlerException::class)
    fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int)

    @Throws(HandlerException::class)
    fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean)

    /** 向房间内所有玩家推送符卡状态 */
    fun pushSpells(room: Room, spellIndex: Int, causer: String)

    /**
     * 获取所有符卡状态
     *
     * @param playerIndex 0:左侧玩家，1:右侧玩家，-1:不是玩家
     */
    fun getAllSpellStatus(room: Room, playerIndex: Int): List<Int> {
        return room.spellStatus!!.toList()
    }
}
