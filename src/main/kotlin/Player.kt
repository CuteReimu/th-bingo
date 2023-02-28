package org.tfcc.bingo

data class Player(
    val token: String,
    val name: String?,
    val roomId: String?,
    var lastOperateMs: Long = 0 // 最后一次操作的时间戳，毫秒
) {
    constructor(token: String) : this(token, null, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        return token.hashCode()
    }
}
