package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.tfcc.bingo.Spell

@Serializable
class DualPageData(
    @SerialName("spells2")
    val spells2: Array<Spell>
) {
    @Transient
    val playerCurrentPage = intArrayOf(0, 0)
}
