package org.tfcc.bingo.message

import org.tfcc.bingo.BpData
import org.tfcc.bingo.LinkData
import org.tfcc.bingo.Spell

class SpellListSc(
    val spells: Array<Spell>,
    val time: Long,
    val startTime: Long,
    val whoseTurn: Int,
    val banPick: Int,
    val totalPauseTime: Long,
    val pauseBeginMs: Long,
    val pauseEndMs: Long,
    val status: IntArray?,
    val phase: Int,
    val linkData: LinkData?,
    val bpData: BpData?,
    val lastGetTime: LongArray,
)
