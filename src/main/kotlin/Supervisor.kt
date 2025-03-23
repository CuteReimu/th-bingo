package org.tfcc.bingo

import io.netty.channel.Channel

/** 用以记录channel和player的对应关系，非线程安全 */
object Supervisor {
    private val channelIdToPlayer = HashMap<String, String>()
    private val playerNameToChannel = HashMap<String, Channel>()

    fun put(channel: Channel, playerName: String) {
        channelIdToPlayer[channel.id().asLongText()] = playerName
        playerNameToChannel[playerName] = channel
    }

    fun removeChannel(channel: Channel): String? {
        val channelId = channel.id().asLongText()
        val token = channelIdToPlayer[channelId]
        if (playerNameToChannel[token] !== channel) return null
        playerNameToChannel.remove(token)
        channelIdToPlayer.remove(channelId)
        return token
    }

    fun getPlayerName(channel: Channel): String? {
        return channelIdToPlayer[channel.id().asLongText()]
    }

    fun getChannel(playerName: String): Channel? {
        return playerNameToChannel[playerName]
    }
}
