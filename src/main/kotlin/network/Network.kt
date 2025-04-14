package org.tfcc.bingo.network

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.apache.logging.log4j.kotlin.logger

object Network {
    fun onInit() {
        initGameNetwork()
    }

    private fun initGameNetwork() {
        val bossGroup = NioEventLoopGroup()
        val workGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap().group(bossGroup, workGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(WebSocketServerInitializer())
            val channelFuture = bootstrap.bind(9999).sync()
            channelFuture.addListener(object : ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    logger.debug(if (channelFuture.isSuccess) "监听端口 9999 成功" else "监听端口 9998 失败")
                }
            })
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workGroup.shutdownGracefully()
        }
    }
}
