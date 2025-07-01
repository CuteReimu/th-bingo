package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import org.tfcc.bingo.*
import java.util.*

object StartGameHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        !room.started || throw HandlerException("游戏已经开始")
        room.players.all { it != null } || throw HandlerException("玩家没满")
        // 执行随机符卡与状态设定
        room.type.resetData(room)
        room.type.rollSpellCard(room)
        room.type.initStatus(room)
        // 后处理
        room.type.onStart(room)
        room.push("push_start_game", room.roomConfig.encode())
        return null
    }
}
