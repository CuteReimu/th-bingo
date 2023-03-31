package org.tfcc.bingo

import io.netty.channel.Channel
import io.netty.channel.ChannelId

/** 用以记录channel和player的对应关系，非线程安全 */
object Supervisor {
    private val channelIdToPlayer = HashMap<ChannelId, String>()
    private val playerTokenToChannel = HashMap<String, Channel>()

    fun put(channel: Channel, playerToken: String) {
        channelIdToPlayer[channel.id()] = playerToken
        playerTokenToChannel[playerToken] = channel
    }

    fun removeChannel(channel: Channel): String? {
        val channelId = channel.id()
        val token = channelIdToPlayer[channelId]
        if (playerTokenToChannel[token] !== channel) return null
        playerTokenToChannel.remove(token)
        channelIdToPlayer.remove(channelId)
        return token
    }

    fun getPlayerToken(channelId: ChannelId): String? {
        return channelIdToPlayer[channelId]
    }

    fun getChannel(playerToken: String): Channel? {
        return playerTokenToChannel[playerToken]
    }
}