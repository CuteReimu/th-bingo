package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

sealed interface RoomType {
    fun onStart(room: Room) {
    }

    @Throws(HandlerException::class)
    fun handleNextRound(room: Room) {
        throw HandlerException("不支持下一回合的游戏类型")
    }

    fun canPause(): Boolean

    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>): Array<Spell>

    @Throws(HandlerException::class)
    fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus
}
