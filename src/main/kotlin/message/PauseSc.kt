package org.tfcc.bingo.message

data class PauseSc(
    val time: Long,
    val totalPauseTime: Long,
    val pauseBeginMs: Long
)
