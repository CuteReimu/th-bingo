package org.tfcc.bingo

/**
 * @param lastOperateMs 最后一次操作的时间戳，毫秒，业务逻辑中请勿修改此字段
 */
class Player(
    val name: String,
    val pwd: String,
    var room: Room? = null,
    var lastOperateMs: Long = 0
)
