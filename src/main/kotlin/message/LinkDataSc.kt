package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData

class LinkDataSc(
    val linkIdxA: IntArray,
    val linkIdxB: IntArray,
    val startMsA: Long,
    val endMsA: Long,
    val startMsB: Long,
    val endMsB: Long
) {
    constructor(data: LinkData) : this(
        data.linkIdxA.toIntArray(),
        data.linkIdxB.toIntArray(),
        data.startMsA,
        data.endMsA,
        data.startMsB,
        data.endMsB
    )
}
