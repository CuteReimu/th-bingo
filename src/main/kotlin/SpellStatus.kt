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
        else -> throw HandlerException("status不合法")
    }
}

enum class SpellStatus(val value: Int) {
    NONE(0), BANNED(-1),
    LEFT_SELECT(1), BOTH_SELECT(2), RIGHT_SELECT(3),
    LEFT_GET(5), BOTH_GET(6), RIGHT_GET(7);

    fun isSelectStatus(): Boolean {
        return this == LEFT_SELECT || this == RIGHT_SELECT || this == BOTH_SELECT
    }

    fun isLeftStatus(): Boolean {
        return this == LEFT_SELECT || this == LEFT_GET || this == BOTH_SELECT || this == BOTH_GET
    }

    fun isRightStatus(): Boolean {
        return this == RIGHT_SELECT || this == RIGHT_GET || this == BOTH_SELECT || this == BOTH_GET
    }
}