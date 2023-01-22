package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class UpdateNameCs(val name: String) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        TODO("Not yet implemented")
    }

}
