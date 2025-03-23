package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.tfcc.bingo.Room

@Serializable
class RoomInfo(
    val rid: String,
    val type: Int,
    val host: String?,
    val names: List<String?>,
    @SerialName("change_card_count")
    val changeCardCount: IntArray,
    val started: Boolean,
    val score: IntArray,
    val watchers: List<String>,
    @SerialName("last_winner")
    val lastWinner: Int,
)

val Room.roomInfo: RoomInfo
    get() = RoomInfo(
        rid = roomId,
        type = roomConfig.type,
        host = host?.name,
        names = players.map { it?.name },
        changeCardCount = changeCardCount,
        started = started,
        score = score,
        watchers = watchers.map { it.name },
        lastWinner = lastWinner,
    )
