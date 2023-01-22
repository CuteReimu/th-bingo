package org.tfcc.bingo.message

data class RoomInfoSc(
    val rid: String,
    val type: Int,
    val host: String,
    val names: Array<String>?,
    val changeCardCount: Array<UInt>?,
    val started: Boolean?,
    val score: Array<UInt>?,
    val winner: Int?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomInfoSc

        if (rid != other.rid) return false
        if (type != other.type) return false
        if (host != other.host) return false
        if (!names.contentEquals(other.names)) return false
        if (!changeCardCount.contentEquals(other.changeCardCount)) return false
        if (started != other.started) return false
        if (!score.contentEquals(other.score)) return false
        if (winner != other.winner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rid.hashCode()
        result = 31 * result + type
        result = 31 * result + host.hashCode()
        result = 31 * result + names.contentHashCode()
        result = 31 * result + changeCardCount.contentHashCode()
        result = 31 * result + started.hashCode()
        result = 31 * result + score.contentHashCode()
        result = 31 * result + winner.hashCode()
        return result
    }
}
