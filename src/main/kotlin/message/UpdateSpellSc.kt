package org.tfcc.bingo.message

class UpdateSpellSc(
    val idx: Int,
    val status: Int,
    val whoseTurn: Int,
    val banPick: Int,
    val isReset: Boolean
)
