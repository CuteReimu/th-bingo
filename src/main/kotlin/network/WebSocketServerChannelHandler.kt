package org.tfcc.bingo.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.Dispatcher
import org.tfcc.bingo.Supervisor
import java.net.SocketException

class WebSocketServerChannelHandler : SimpleChannelInboundHandler<WebSocketFrame>() {
    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        handlerWebSocketFrame(ctx, msg)
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        // 添加连接
        logger.debug("客户端加入连接：${ctx.channel()}")
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        // 断开连接
        logger.debug("客户端断开连接：${ctx.channel()}")
        Supervisor.removeChannel(ctx.channel())
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    @Deprecated("Deprecated in Java")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is SocketException && "Connection reset" == cause.message) return
        super.exceptionCaught(ctx, cause)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (evt is IdleStateEvent && evt.state() == IdleState.READER_IDLE) {
            logger.debug("客户端心跳超时：${ctx?.channel()}")
            ctx?.channel()?.close()
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

    private fun handlerWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        if (frame !is TextWebSocketFrame) {
            logger.debug("仅支持文本消息，不支持二进制消息")
            throw UnsupportedOperationException("${frame.javaClass.name} frame types not supported")
        }
        // 返回应答消息
        val request = frame.text()
        Dispatcher.handle(ctx, request)
    }
}
