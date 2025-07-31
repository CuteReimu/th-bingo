package org.tfcc.bingo

import org.tfcc.bingo.message.HandlerException

@Throws(HandlerException::class)
fun Int.toSpellStatus(): SpellStatus {
    return when (this) {
        -1 -> SpellStatus.BANNED
        0 -> SpellStatus.NONE
        1 -> SpellStatus.LEFT_SELECT
        2 -> SpellStatus.BOTH_SELECT
        3 -> SpellStatus.RIGHT_SELECT
        5 -> SpellStatus.LEFT_GET
        6 -> SpellStatus.BOTH_GET
        7 -> SpellStatus.RIGHT_GET
        0x1000 -> SpellStatus.BOTH_HIDDEN
        0x1001 -> SpellStatus.LEFT_SEE_ONLY
        0x1002 -> SpellStatus.RIGHT_SEE_ONLY
        0x1010 -> SpellStatus.ONLY_REVEAL_GAME
        0x1011 -> SpellStatus.ONLY_REVEAL_GAME_STAGE
        0x1012 -> SpellStatus.ONLY_REVEAL_STAR
        else -> throw HandlerException("status不合法")
    }
}

enum class SpellStatus(val value: Int) {
    NONE(0),
    BANNED(-1),
    LEFT_SELECT(1),
    BOTH_SELECT(2),
    RIGHT_SELECT(3),
    LEFT_GET(5),
    BOTH_GET(6),
    RIGHT_GET(7),
    BOTH_HIDDEN(0x1000),
    LEFT_SEE_ONLY(0x1001),
    RIGHT_SEE_ONLY(0x1002),
    ONLY_REVEAL_GAME(0x1010),
    ONLY_REVEAL_GAME_STAGE(0x1011),
    ONLY_REVEAL_STAR(0x1012);

    fun isSelectStatus(): Boolean {
        return this == LEFT_SELECT || this == RIGHT_SELECT || this == BOTH_SELECT
    }

    fun isGetStatus(): Boolean {
        return this == LEFT_GET || this == RIGHT_GET || this == BOTH_GET
    }

    fun isEmptyStatus(): Boolean {
        return this == NONE || this == BOTH_HIDDEN || this == LEFT_SEE_ONLY || this == RIGHT_SEE_ONLY ||
            this == ONLY_REVEAL_GAME || this == ONLY_REVEAL_GAME_STAGE || this == ONLY_REVEAL_STAR
    }

    fun isOneSelectStatus(): Boolean {
        return this == LEFT_SELECT || this == RIGHT_SELECT
    }

    /** 左右颠倒，方便计算 */
    fun opposite(): SpellStatus = when (this) {
        LEFT_GET -> RIGHT_GET
        RIGHT_GET -> LEFT_GET
        LEFT_SELECT -> RIGHT_SELECT
        RIGHT_SELECT -> LEFT_SELECT
        LEFT_SEE_ONLY -> RIGHT_SEE_ONLY
        RIGHT_SEE_ONLY -> LEFT_SEE_ONLY
        else -> this
    }
}
