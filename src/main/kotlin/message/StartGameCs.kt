package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class StartGameCs(
    val gameTime: UInt, // 游戏总时间（不含倒计时），单位：分
    val countdown: UInt, // 倒计时，单位：秒
    val games: Array<String>,
    val ranks: Array<String>,
    val needWin: UInt
) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StartGameCs

        if (gameTime != other.gameTime) return false
        if (countdown != other.countdown) return false
        if (!games.contentEquals(other.games)) return false
        if (!ranks.contentEquals(other.ranks)) return false
        if (needWin != other.needWin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameTime.hashCode()
        result = 31 * result + countdown.hashCode()
        result = 31 * result + games.contentHashCode()
        result = 31 * result + ranks.contentHashCode()
        result = 31 * result + needWin.hashCode()
        return result
    }
}
