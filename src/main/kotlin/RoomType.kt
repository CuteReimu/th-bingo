package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

sealed interface RoomType {
    fun onStart(room: Room) {
    }

    @Throws(HandlerException::class)
    fun handleNextRound(room: Room) {
    }

    fun canPause(): Boolean

    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>): Array<Spell>

    @Throws(HandlerException::class)
    fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus
}
