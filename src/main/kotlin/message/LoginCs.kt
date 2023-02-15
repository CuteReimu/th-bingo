package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor

data class LoginCs(val token: String?) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (this.token.isNullOrEmpty() || this.token.toByteArray().size > 128 || !this.token.isLetterOrDigit()) {
            ctx.writeMessage(Message(protoName, ErrorSc(400, "invalid token")))
            return
        }
        if (Supervisor.getChannel(this.token) != null) {
            throw HandlerException("already online")
        }
        if (Store.getPlayer(this.token) == null) {
            Store.putPlayer(Player(this.token))
        }
        Supervisor.add(ctx.channel(), this.token)
        val msg = Store.buildPlayerInfo(this.token)
        ctx.writeMessage(Message(name = msg.name, reply = protoName, trigger = msg.trigger, data = msg.data))
    }

    private fun String.isLetterOrDigit(): Boolean {
        for (c in this) {
            if (!c.isLetterOrDigit()) {
                return false
            }
        }
        return true
    }
}