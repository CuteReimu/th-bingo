package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.BanPick
import org.tfcc.bingo.Player
import org.tfcc.bingo.RequestHandler

object StartBanPickHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val whoFirst = m["who_first"]!!.jsonPrimitive.int
        if (whoFirst != 0 && whoFirst != 1) throw HandlerException("参数错误")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        !room.started || throw HandlerException("游戏已经开始")
        room.players.all { it != null } || throw HandlerException("玩家没满")
        if (room.banPick != null && room.banPick!!.phase != 9999) throw HandlerException("正在BP环节")
        val bp = BanPick(whoFirst)
        room.banPick = bp
        room.locked = true
        bp.notifyAll(room)
        return null
    }
}
