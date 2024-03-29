package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor
import org.tfcc.bingo.toSpellStatus

class UpdateSpellCs(
    val idx: Int,
    val status: Int,
    val isReset: Boolean = false,
    val controlRobot: Boolean = false
) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (idx < 0 || idx >= 25) throw HandlerException("idx超出范围")
        val spellStatus = status.toSpellStatus()
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.started) throw HandlerException("游戏还没开始")
        if (room.host != token && !room.players.contains(token)) throw HandlerException("没有权限")
        if (controlRobot) {
            if (room.players[0] != token)
                throw HandlerException("你不能控制${Store.robotPlayer.name}")
            if (room.players[1] != Store.robotPlayer.token)
                throw HandlerException("找不到${Store.robotPlayer.name}")
        }
        val now = System.currentTimeMillis()
        val playerToken = if (controlRobot) Store.robotPlayer.token else token
        val newStatus = room.type.handleUpdateSpell(room, playerToken, idx, spellStatus, now, isReset)
        room.spellStatus!![idx] = newStatus
        val playerIndex = room.players.indexOf(playerToken)
        if (playerIndex >= 0 && spellStatus.isGetStatus())
            room.lastGetTime[playerIndex] = now - room.totalPauseMs - room.startMs
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
                            room.bpData?.banPick ?: 0,
                            isReset
                        )
                    )
                )
            }
        }
    }
}
