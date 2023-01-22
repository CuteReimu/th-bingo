package org.tfcc.bingo

import java.io.Serializable

data class Player(val token: String, var name: String?, var roomId: String?) : Serializable {
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
