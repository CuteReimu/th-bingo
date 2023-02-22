package org.tfcc.bingo.message

data class RoomInfoSc(
    val rid: String,
    val type: Int,
    val host: String,
    val names: Array<String>?,
    val changeCardCount: IntArray?,
    val started: Boolean?,
    val score: IntArray?,
    val winner: Int?,
    val watchers: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomInfoSc

        if (rid != other.rid) return false
        if (type != other.type) return false
        if (host != other.host) return false
        if (names != null) {
            if (other.names == null) return false
            if (!names.contentEquals(other.names)) return false
        } else if (other.names != null) return false
        if (changeCardCount != null) {
            if (other.changeCardCount == null) return false
            if (!changeCardCount.contentEquals(other.changeCardCount)) return false
        } else if (other.changeCardCount != null) return false
        if (started != other.started) return false
        if (score != null) {
            if (other.score == null) return false
            if (!score.contentEquals(other.score)) return false
        } else if (other.score != null) return false
        if (winner != other.winner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rid.hashCode()
        result = 31 * result + type
        result = 31 * result + host.hashCode()
        result = 31 * result + (names?.contentHashCode() ?: 0)
        result = 31 * result + (changeCardCount?.contentHashCode() ?: 0)
        result = 31 * result + (started?.hashCode() ?: 0)
        result = 31 * result + (score?.contentHashCode() ?: 0)
        result = 31 * result + (winner ?: 0)
        return result
    }
}
