package org.tfcc.bingo.network

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.util.concurrent.GlobalEventExecutor
import java.util.concurrent.ConcurrentHashMap


object ChannelSupervise {
    private val GlobalGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    private val ChannelMap = ConcurrentHashMap<String?, ChannelId?>()
    fun addChannel(channel: Channel) {
        GlobalGroup.add(channel)
        ChannelMap[channel.id().asShortText()] = channel.id()
    }

    fun removeChannel(channel: Channel) {
        GlobalGroup.remove(channel)
        ChannelMap.remove(channel.id().asShortText())
    }

    fun findChannel(id: String?): Channel {
        return GlobalGroup.find(ChannelMap[id])
    }

    fun send2All(tws: TextWebSocketFrame?) {
        GlobalGroup.writeAndFlush(tws)
    }
}