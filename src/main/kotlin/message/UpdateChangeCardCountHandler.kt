package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.*
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push

object UpdateChangeCardCountHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val name = m["name"]!!.jsonPrimitive.content
        val count = m["count"]!!.jsonPrimitive.int
        count in 0..9999 || throw HandlerException("cnt参数错误")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        val index = room.players.indexOfFirst { it?.name == name }
        index >= 0 || throw HandlerException("找不到目标玩家")
        room.changeCardCount[index] = count
        room.push(
            "push_update_change_card_count", JsonObject(
                mapOf(
                    "name" to JsonPrimitive(name),
                    "count" to JsonPrimitive(count),
                )
            )
        )
        return null
    }
}
