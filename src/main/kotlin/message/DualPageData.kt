package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.tfcc.bingo.Spell

@Serializable
class DualPageData(
    @SerialName("spells2")
    val spells2: Array<Spell>
) {
    @SerialName("player_current_page")
    val playerCurrentPage = intArrayOf(0, 0)
}
