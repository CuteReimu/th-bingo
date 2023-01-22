package org.tfcc.bingo.message

data class Message(
    val name: String?,
    val reply: String?,
    val trigger: String?,
    val data: Any?
) {
    constructor(data: Any?) : this(null, null, null, data)
    constructor(reply: String?, data: Any?) : this(null, reply, null, data)
    constructor(reply: String?, trigger: String?, data: Any?) : this(null, reply, trigger, data)
}