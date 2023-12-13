package org.tfcc.bingo.message

class PauseSc(
    val time: Long,
    val totalPauseTime: Long,
    val pauseBeginMs: Long,
    val pauseEndMs: Long,
    val lastGetTime: LongArray
)
