package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class UpdateRoomTypeCs(val type: Int) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        TODO("Not yet implemented")
    }

}
