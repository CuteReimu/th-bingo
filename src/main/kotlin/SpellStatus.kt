package org.tfcc.bingo

fun Int.isSelectStatus(): Boolean {
    return this % 100 == 1 || this / 100 == 1
}

fun Int.isGetStatus(): Boolean {
    return this % 100 == 2 || this / 100 == 2
}

fun Int.opposite(): Int {
    return this % 100 * 100 + this / 100
}

object SpellStatus {
    const val NONE = 0
    const val BANNED = 303
    const val LEFT_SELECT = 100
    const val BOTH_SELECT = 101
    const val RIGHT_SELECT = 1
    const val LEFT_GET = 200
    const val BOTH_GET = 202
    const val RIGHT_GET = 2
}
