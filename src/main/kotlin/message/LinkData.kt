package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LinkData {
    @SerialName("link_idx_a")
    val linkIdxA = ArrayList<Int>()

    @SerialName("link_idx_b")
    val linkIdxB = ArrayList<Int>()

    @SerialName("start_ms_a")
    var startMsA = 0L

    @SerialName("end_ms_a")
    var endMsA = 0L

    @SerialName("event_a")
    var eventA = 0

    @SerialName("start_ms_b")
    var startMsB = 0L

    @SerialName("end_ms_b")
    var endMsB = 0L

    @SerialName("event_b")
    var eventB = 0

    fun selectCompleteA() = (eventA > 0 || eventB > 0) && linkIdxA.last() == 24
    fun selectCompleteB() = (eventA > 0 || eventB > 0) && linkIdxB.last() == 20
}
