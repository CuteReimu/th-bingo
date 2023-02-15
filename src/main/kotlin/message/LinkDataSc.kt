package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData

data class LinkDataSc(
    val linkIdxA: IntArray,
    val linkIdxB: IntArray,
    val startMsA: Long,
    val endMsA: Long,
    val startMsB: Long,
    val endMsB: Long
) {
    constructor(data: LinkData) : this(
        IntArray(data.linkIdxA.size),
        IntArray(data.linkIdxB.size),
        data.startMsA,
        data.endMsA,
        data.startMsB,
        data.endMsB
    ) {
        for ((i, v) in data.linkIdxA.withIndex()) linkIdxA[i] = v
        for ((i, v) in data.linkIdxB.withIndex()) linkIdxB[i] = v
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinkDataSc

        if (!linkIdxA.contentEquals(other.linkIdxA)) return false
        if (!linkIdxB.contentEquals(other.linkIdxB)) return false
        if (startMsA != other.startMsA) return false
        if (endMsA != other.endMsA) return false
        if (startMsB != other.startMsB) return false
        if (endMsB != other.endMsB) return false

        return true
    }

    override fun hashCode(): Int {
        var result = linkIdxA.contentHashCode()
        result = 31 * result + linkIdxB.contentHashCode()
        result = 31 * result + startMsA.hashCode()
        result = 31 * result + endMsA.hashCode()
        result = 31 * result + startMsB.hashCode()
        result = 31 * result + endMsB.hashCode()
        return result
    }

}
