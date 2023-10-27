package org.tfcc.bingo.message

class RoomInfoSc(
    val rid: String,
    val type: Int,
    val host: String,
    val names: Array<String>?,
    val changeCardCount: IntArray?,
    val started: Boolean?,
    val score: IntArray?,
    val winner: Int?,
    val watchers: Array<String>?,
    val roomConfig: RoomConfig,
)
