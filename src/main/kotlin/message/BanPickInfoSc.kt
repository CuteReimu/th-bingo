package org.tfcc.bingo.message

class BanPickInfoSc(
    val whoFirst: Int,
    val phase: Int,
    val aPick: Array<String>,
    val aBan: Array<String>,
    val bPick: Array<String>,
    val bBan: Array<String>,
    val aOpenEx: Int,
    val bOpenEx: Int,
)