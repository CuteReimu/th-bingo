package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData

data class LinkDataSc(
    val linkIdxA: Array<UInt>,
    val linkIdxB: Array<UInt>,
    val startMsA: Long,
    val endMsA: Long,
    val startMsB: Long,
    val endMsB: Long
) {
    constructor(data: LinkData) : this(
        data.linkIdxA.toArray(arrayOf<UInt>()) ?: arrayOf<UInt>(),
        data.linkIdxB.toArray(arrayOf<UInt>()) ?: arrayOf<UInt>(),
        data.startMsA,
        data.endMsA,
        data.startMsB,
        data.endMsB
    )

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
