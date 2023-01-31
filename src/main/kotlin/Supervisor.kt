package org.tfcc.bingo

import io.netty.channel.Channel
import io.netty.channel.ChannelId

// 用以记录channel和player的对应关系，非线程安全
object Supervisor {
    private val channelIdToPlayer = HashMap<ChannelId, String>()
    private val playerTokenToChannel = HashMap<String, Channel>()

    fun add(channel: Channel, playerToken: String) {
        channelIdToPlayer[channel.id()] = playerToken
        playerTokenToChannel[playerToken] = channel
    }

    fun removeByPlayerToken(channelId: ChannelId): String? {
        val token = channelIdToPlayer.remove(channelId)
        playerTokenToChannel.remove(token)
        return token
    }

    fun getPlayerToken(channelId: ChannelId): String? {
        return channelIdToPlayer[channelId]
    }

    fun getChannel(playerToken: String): Channel? {
        return playerTokenToChannel[playerToken]
    }
}