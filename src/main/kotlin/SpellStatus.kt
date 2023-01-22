package org.tfcc.bingo

enum class SpellStatus(val value: Int) {
    NONE(0), BANNED(-1),
    LEFT_SELECT(1), BOTH_SELECT(2), RIGHT_SELECT(3),
    LEFT_GET(5), BOTH_GET(6), RIGHT_GET(7);
}