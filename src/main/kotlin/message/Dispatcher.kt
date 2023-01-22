package org.tfcc.bingo.message

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.apache.log4j.Logger
import org.tfcc.bingo.Supervisor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

fun ChannelHandlerContext.writeMessage(message: Message): ChannelFuture {
    val text = Dispatcher.gson.toJson(
        if (message.name == null && message.data != null) {
            Message(
                Dispatcher.camelToUnderLine(message.data.javaClass.simpleName),
                message.reply,
                message.trigger,
                message.data
            )
        } else message
    )
    Dispatcher.logger.debug("服务器发给${channel().id().asShortText()}：$text")
    return writeAndFlush(TextWebSocketFrame(text))
}

fun Channel.writeMessage(message: Message): ChannelFuture {
    val text = Dispatcher.gson.toJson(
        if (message.name == null && message.data != null) {
            Message(
                Dispatcher.camelToUnderLine(message.data.javaClass.simpleName),
                message.reply,
                message.trigger,
                message.data
            )
        } else message
    )
    Dispatcher.logger.debug("服务器发给${id().asShortText()}：$text")
    return writeAndFlush(TextWebSocketFrame(text))
}

object Dispatcher {
    val logger: Logger = Logger.getLogger(Dispatcher.javaClass)
    private val pool = Executors.newSingleThreadExecutor()
    private val cache = ConcurrentHashMap<String, Class<Handler>>()
    val gson = Gson()

    fun handle(ctx: ChannelHandlerContext, text: String) {
        try {
            val m = gson.fromJson(text, Message::class.java)
            if (m.name == null) {
                logger.warn("unknown message")
                ctx.writeMessage(Message(ErrorSc(400, "illegal request")))
                return
            }
            var cls = cache[m.name]
            if (cls == null) {
                try {
                    val cls1 = Class.forName("org.tfcc.bingo.message.${underlineToCamel(m.name)}")
                    if (cls1.interfaces.contains(Handler::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        cls = cls1 as Class<Handler>
                        cache.putIfAbsent(m.name, cls)
                    }
                } catch (_: ClassNotFoundException) {
                }
            }
            if (cls == null) {
                logger.warn("can not find handler: ${m.name}")
                ctx.writeMessage(Message(ErrorSc(404, "404 not found")))
                return
            }
            pool.submit {
                var token: String? = null
                if (m.name != "login_cs") {
                    token = Supervisor.getPlayerToken(ctx.channel().id())
                    if (token == null) {
                        ctx.writeMessage(Message("error_sc", m.name, null, ErrorSc(-1, "You haven't login")))
                        return@submit
                    }
                }
                val handler = gson.fromJson(gson.toJson(m.data), cls)
                try {
                    handler.handle(ctx, token, m.name)
                } catch (e: HandlerException) {
                    logger.error("handle failed: ${m.name}, error: ", e)
                    ctx.writeMessage(Message(m.name, ErrorSc(500, e.message)))
                }
            }
        } catch (e: JsonSyntaxException) {
            ctx.writeMessage(Message("error_sc", null, null, ErrorSc(400, "illegal json")))
        }
    }

    fun camelToUnderLine(s: String): String {
        val sb = StringBuilder()
        for ((i, c) in s.withIndex()) {
            if (i > 0 && c.isUpperCase()) {
                sb.append('_')
            }
            sb.append(c.lowercaseChar())
        }
        return sb.toString()
    }

    private fun underlineToCamel(s: String): String {
        val sb = StringBuilder()
        var upper = true
        for (c in s) {
            upper = if (c == '_') {
                true
            } else {
                sb.append(if (upper) c.uppercaseChar() else c)
                false
            }
        }
        return sb.toString()
    }
}