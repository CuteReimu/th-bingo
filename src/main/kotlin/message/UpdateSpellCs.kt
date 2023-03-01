package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*

data class UpdateSpellCs(val idx: Int, val status: Int) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (idx < 0 || idx >= 25) throw HandlerException("idx超出范围")
        val spellStatus = status.toSpellStatus()
        if (spellStatus == SpellStatus.BOTH_SELECT) throw HandlerException("status不合法")
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
        if (!room.started) throw HandlerException("游戏还没开始")
        if (room.host != player.token && !room.players.contains(player.token)) throw HandlerException("没有权限")
        val newStatus = room.type.handleUpdateSpell(room, player.token, idx, spellStatus)
        room.spellStatus!![idx] = newStatus
        Store.putRoom(room)
        for (token1 in Store.getAllPlayersInRoom(player.token) ?: return) {
            if (token1.isNotEmpty()) {
                val conn = Supervisor.getChannel(token1) ?: continue
                conn.writeMessage(
                    Message(
                        if (token1 == player.token) protoName else null,
                        player.name,
                        UpdateSpellSc(idx, newStatus.value, room.bpData?.whoseTurn ?: 0, room.bpData?.banPick ?: 0)
                    )
                )
            }
        }
    }
}
