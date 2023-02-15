package org.tfcc.bingo.message

data class UpdateSpellSc(
    val idx: Int,
    val status: Int,
    val whoseTurn: Int,
    val banPick: Int
)
