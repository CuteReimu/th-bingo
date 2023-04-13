package org.tfcc.bingo

class LinkData {
    var linkIdxA = ArrayList<Int>()
    var linkIdxB = ArrayList<Int>()
    var startMsA = 0L
    var endMsA = 0L
    var startMsB = 0L
    var endMsB = 0L

    fun selectCompleteA() = (startMsA > 0 || startMsB > 0) && linkIdxA.last() == 24
    fun selectCompleteB() = (startMsA > 0 || startMsB > 0) && linkIdxB.last() == 20
}
