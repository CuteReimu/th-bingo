package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

class HandlerException(msg: String) : Exception(msg)

interface Handler {
    @Throws(HandlerException::class)
    fun handle(ctx: ChannelHandlerContext, token: String, protoName: String)
}