package org.tfcc.bingo.message

data class Message(
    val name: String? = null,
    val reply: String? = null,
    val trigger: String? = null,
    val data: Any? = null
)