package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Room
import org.tfcc.bingo.Store

data class CreateRoomCs(
    val name: String,
    val rid: String,
    val type: Int
) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (name.isEmpty()) throw HandlerException("名字为空")
        if (name.toByteArray().size > 48) throw HandlerException("名字太长")
        if (rid.isEmpty()) throw HandlerException("房间ID为空")
        if (rid.toByteArray().size > 16) throw HandlerException("房间ID太长")
        if (type < 1 || type > 3) throw HandlerException("不支持的游戏类型")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId != null && Store.getRoom(player.roomId) != null) throw HandlerException("已经在房间里了")
        if (Store.getRoom(rid) != null) throw HandlerException("房间已存在")
        Store.putPlayer(Player(token = token, roomId = rid, name = name))
        Store.putRoom(
            Room(
                roomId = rid,
                roomType = type,
                host = token,
                players = arrayOf("", ""),
                started = false,
                spells = null,
                startMs = 0,
                gameTime = 0U,
                countDown = 0U,
                spellStatus = null,
                score = arrayOf(0U, 0U),
                locked = false,
                needWin = 0U,
                changeCardCount = arrayOf(0U, 0U),
                totalPauseMs = 0,
                pauseBeginMs = 0,
                lastWinner = 0,
                bpData = null,
                linkData = null,
                phase = 0
            )
        )
        Store.notifyPlayerInfo(token, protoName)
    }
}
