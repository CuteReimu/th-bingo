package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor

data class LoginCs(val token: String?) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (this.token.isNullOrEmpty() || !this.token.matches(Regex("[a-z0-9]{1,100}"))) {
            ctx.writeMessage(Message(protoName, ErrorSc(400, "invalid token")))
            return
        }
        if (Supervisor.getChannel(this.token) != null) {
            throw HandlerException("already online")
        }
        if (player == null) {
            Store.putPlayer(Player(this.token))
        }
        Supervisor.add(ctx.channel(), this.token)
        val msg = Store.buildPlayerInfo(this.token)
        ctx.writeMessage(Message(name = msg.name, reply = protoName, trigger = msg.trigger, data = msg.data))
    }
}