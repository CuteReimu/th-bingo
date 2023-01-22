package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class PauseCs(val pause: Boolean) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        TODO("Not yet implemented")
    }

}
