package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
    fun randSpells(games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell>

    @Throws(HandlerException::class)
    fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int)

    @Throws(HandlerException::class)
    fun handleFinishSpell(room: Room, playerIndex: Int, spellIndex: Int)

    /** 向房间内所有玩家推送符卡状态 */
    fun pushSpells(room: Room, spellIndex: Int, causer: String) {
        val status = room.spellStatus!![spellIndex]
        room.push(
            "push_update_spell_status", JsonObject(
                mapOf(
                    "index" to JsonPrimitive(spellIndex),
                    "status" to JsonPrimitive(status.value),
                    "causer" to JsonPrimitive(causer),
                )
            )
        )
    }
}
