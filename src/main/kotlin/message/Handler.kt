package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room

class HandlerException(msg: String) : Exception(msg)

interface Handler {
    @Throws(HandlerException::class)
    fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String)
}