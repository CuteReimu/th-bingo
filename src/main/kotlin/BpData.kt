package org.tfcc.bingo

data class BpData(
    var whoseTurn: Int,
    var banPick: Int,
    var round: UInt,
    var lessThan4: Boolean
)
