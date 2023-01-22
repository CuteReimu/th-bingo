package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext

data class ChangeCardCountCs(val counts: Array<UInt>) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String?, protoName: String) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeCardCountCs

        if (!counts.contentEquals(other.counts)) return false

        return true
    }

    override fun hashCode(): Int {
        return counts.contentHashCode()
    }

}
