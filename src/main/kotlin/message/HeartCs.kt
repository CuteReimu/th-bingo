package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

class HeartCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        ctx.writeMessage(Message(protoName, HeartSc(System.currentTimeMillis())))
    }
}
