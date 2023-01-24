package org.tfcc.bingo.message

import org.tfcc.bingo.SpellStatus

data class UpdateSpellSc(
    val idx: Int,
    val status: SpellStatus,
    val whoseTurn: Int,
    val banPick: Int
)
