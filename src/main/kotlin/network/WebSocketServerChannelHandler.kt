package org.tfcc.bingo.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import org.apache.log4j.Logger
import org.tfcc.bingo.Supervisor
import org.tfcc.bingo.message.Dispatcher
import org.tfcc.bingo.message.LeaveRoomCs
import java.util.*


class WebSocketServerChannelHandler : SimpleChannelInboundHandler<WebSocketFrame>() {
    private val logger = Logger.getLogger(this.javaClass)

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        logger.debug("收到消息：$msg")
        handlerWebSocketFrame(ctx, msg)
    }

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        //添加连接
        logger.debug("客户端加入连接：" + ctx.channel())
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        //断开连接
        logger.debug("客户端断开连接：" + ctx.channel())
        val token = Supervisor.removeByPlayerToken(ctx.channel().id()) ?: return
        LeaveRoomCs().handle(ctx, token, "")
    }

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    private fun handlerWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        if (frame !is TextWebSocketFrame) {
            logger.debug("仅支持文本消息，不支持二进制消息")
            throw UnsupportedOperationException("${frame.javaClass.name} frame types not supported")
        }
        // 返回应答消息
        val request = frame.text()
        logger.debug("收到${ctx.channel().id().asShortText()}：$request")
        Dispatcher.handle(ctx, request)
    }
}
