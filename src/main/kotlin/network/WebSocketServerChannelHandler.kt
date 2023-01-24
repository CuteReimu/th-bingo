package org.tfcc.bingo.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import org.apache.log4j.Logger
import org.tfcc.bingo.message.Dispatcher
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
        ChannelSupervise.addChannel(ctx.channel())
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        //断开连接
        logger.debug("客户端断开连接：" + ctx.channel())
        ChannelSupervise.removeChannel(ctx.channel())
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
        logger.debug("服务端收到${ctx.channel().id().asShortText()}：$request")
        Dispatcher.handle(ctx, request)
//        val tws = TextWebSocketFrame("${Date()}${ctx.channel().id()}：$request")
        // 返回
//        ctx.channel().writeAndFlush(tws)
    }
}
