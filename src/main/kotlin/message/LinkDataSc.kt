package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData

class LinkDataSc(
    val linkIdxA: IntArray,
    val linkIdxB: IntArray,
    val startMsA: Long,
    val endMsA: Long,
    val eventA: Int,
    val startMsB: Long,
    val endMsB: Long,
    val eventB: Int
) {
    constructor(data: LinkData) : this(
        data.linkIdxA.toIntArray(),
        data.linkIdxB.toIntArray(),
        data.startMsA,
        data.endMsA,
        data.eventA,
        data.startMsB,
        data.endMsB,
        data.eventB
    )
}
