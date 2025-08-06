package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler
import org.tfcc.bingo.push

object SwitchPageHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("找不到房间")
        room.started || throw HandlerException("游戏未开始")
        val now = System.currentTimeMillis()
        now < room.startMs + room.roomConfig.countdown || throw HandlerException("游戏已开始，不能切换页面")
        val playerIndex = room.players.indexOf(player)
        playerIndex >= 0 || throw HandlerException("你不是选手")
        val m = data!!.jsonObject
        val page = m["page"]?.jsonPrimitive?.int ?: throw HandlerException("缺少page参数")
        page == 0 || page == 1 || throw HandlerException("page参数错误")
        room.dualPageData != null || throw HandlerException("该模式不支持切换页面")
        room.dualPageData!!.playerCurrentPage[playerIndex] = page
        room.push("push_switch_page", JsonObject(mapOf(
            "player_index" to JsonPrimitive(playerIndex),
            "page" to JsonPrimitive(page),
        )))
        return null
    }
}
