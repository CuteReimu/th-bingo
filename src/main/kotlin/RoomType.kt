package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

sealed interface RoomType {
    val name: String

    fun onStart(room: Room) {
        // Do nothing
    }

    @Throws(HandlerException::class)
    fun handleNextRound(room: Room) {
        throw HandlerException("不支持下一回合的游戏类型")
    }

    val canPause: Boolean

    @Throws(HandlerException::class)
    fun randSpells(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell> {
        var i = 0
        while (true) {
            try {
                return randSpells0(games, ranks, difficulty)
            } catch (e: SpellFactory.SpellNotEnoughException) {
                if (++i == 5) throw e
            }
        }
    }

    @Throws(HandlerException::class)
    fun randSpells0(games: Array<String>, ranks: Array<String>?, difficulty: Difficulty): Array<Spell>

    @Throws(HandlerException::class)
    fun handleUpdateSpell(room: Room, token: String, idx: Int, status: SpellStatus): SpellStatus
}
