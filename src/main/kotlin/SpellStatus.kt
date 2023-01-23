package org.tfcc.bingo

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