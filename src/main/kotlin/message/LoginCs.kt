package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.apache.log4j.Logger
import org.tfcc.bingo.Player
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor

data class LoginCs(val token: String?) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (this.token.isNullOrEmpty() || !this.token.matches(Regex("[a-z0-9]{1,100}"))) {
            ctx.writeMessage(Message(protoName, ErrorSc(400, "invalid token")))
            return
        }
        val oldChannel = Supervisor.getChannel(this.token)
        if (oldChannel != null) {
            logger.warn("already online, kick old session")
            oldChannel.close()
        }
        if (Store.getPlayer(this.token) == null) {
            Store.putPlayer(Player(this.token))
        }
        Supervisor.put(ctx.channel(), this.token)
        val msg = Store.buildPlayerInfo(this.token)
        ctx.writeMessage(Message(name = msg.name, reply = protoName, trigger = msg.trigger, data = msg.data))
    }

    companion object {
        private val logger: Logger = Logger.getLogger(LoginCs::class.java)
    }
}