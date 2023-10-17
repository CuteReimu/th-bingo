package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor
import org.tfcc.bingo.toSpellStatus

class UpdateSpellCs(val idx: Int, val status: Int) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (idx < 0 || idx >= 25) throw HandlerException("idx超出范围")
        val spellStatus = status.toSpellStatus()
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.started) throw HandlerException("游戏还没开始")
        if (room.host != token && !room.players.contains(token)) throw HandlerException("没有权限")
        val now = System.currentTimeMillis()
        val newStatus = room.type.handleUpdateSpell(room, token, idx, spellStatus, now)
        room.spellStatus!![idx] = newStatus
        val playerIndex = room.players.indexOf(token)
        if (playerIndex >= 0 && spellStatus.isGetStatus())
            room.lastGetTime[playerIndex] = now
        Store.putRoom(room)
        for (token1 in Store.getAllPlayersInRoom(token) ?: return) {
            if (token1.isNotEmpty()) {
                val conn = Supervisor.getChannel(token1) ?: continue
                conn.writeMessage(
                    Message(
                        reply = if (token1 == token) protoName else null,
                        trigger = player.name,
                        data = UpdateSpellSc(
                            idx,
                            newStatus.value,
                            room.bpData?.whoseTurn ?: 0,
                            room.bpData?.banPick ?: 0
                        )
                    )
                )
            }
        }
    }
}
