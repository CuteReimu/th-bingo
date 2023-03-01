package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import java.util.*

class HeartCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        ctx.writeMessage(Message(protoName, HeartSc(Date().time)))
    }
}
