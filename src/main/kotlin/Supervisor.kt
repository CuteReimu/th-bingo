package org.tfcc.bingo

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import java.util.concurrent.ConcurrentHashMap

// 用以记录channel和player的对应关系
object Supervisor {
    private val channelIdToPlayer = ConcurrentHashMap<ChannelId, String>()
    private val playerTokenToChannel = ConcurrentHashMap<String, Channel>()

    fun add(channel: Channel, playerToken: String) {
        channelIdToPlayer.compute(channel.id()) { _, _ ->
            playerTokenToChannel[playerToken] = channel
            playerToken
        }
    }

    fun removeByChannelId(channelId: ChannelId): String? {
        return channelIdToPlayer.computeIfPresent(channelId) { _, token ->
            playerTokenToChannel.remove(token)
            null
        }
    }

    fun getPlayerToken(channelId: ChannelId): String? {
        return channelIdToPlayer[channelId]
    }

    fun getChannel(playerToken: String): Channel? {
        return playerTokenToChannel[playerToken]
    }
}