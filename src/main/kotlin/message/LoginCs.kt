package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor

data class LoginCs(val token: String?) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        if (this.token.isNullOrEmpty() || this.token.length > 128 || !this.token.isLetterOrDigit()) {
            ctx.writeMessage(Message(protoName, ErrorSc(400, "invalid token")))
            return
        }
        if (Supervisor.getChannel(this.token) != null) {
            throw HandlerException("already online")
        }
        if (Store.getPlayer(this.token) == null) {
            Store.putPlayer(Player(this.token, null, null))
        }
        Supervisor.add(ctx.channel(), this.token)
        ctx.writeAndFlush(Message(ErrorSc(0, "ok")))
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