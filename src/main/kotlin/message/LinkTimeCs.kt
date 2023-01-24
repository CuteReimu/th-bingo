package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class LinkTimeCs(val whose: Int, val start: Boolean) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        TODO("Not yet implemented")
    }
}
