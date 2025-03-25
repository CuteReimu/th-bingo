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

    /**
     * @param banSelect true-ban操作，false-选卡操作
     */
    @Throws(HandlerException::class)
    fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int)

    @Throws(HandlerException::class)
    fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean)

    /** 向房间内所有玩家推送符卡状态 */
    fun pushSpells(room: Room, spellIndex: Int, causer: String)
}
