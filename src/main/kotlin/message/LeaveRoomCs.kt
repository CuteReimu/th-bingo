package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.Player
import org.tfcc.bingo.Store
import org.tfcc.bingo.Supervisor

class LeaveRoomCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.started) throw HandlerException("比赛已经开始了，不能退出")
        if (room.host != token && room.locked) throw HandlerException("连续比赛没结束，不能退出")
        val tokens = ArrayList<String>()
        val roomDestroyed: Boolean
        if (room.host.isEmpty()) {
            val index = room.players.indexOf(token)
            if (index >= 0) room.players[index] = ""
            room.watchers.remove(token)
            val players = room.players.filter { s -> s.isNotEmpty() }
            tokens.addAll(players)
            tokens.addAll(room.watchers)
            roomDestroyed = players.isEmpty()
        } else if (room.host == token) {
            for (p in room.players) {
                if (p.isNotEmpty()) {
                    tokens.add(p)
                    Store.putPlayer(Player(p))
                }
            }
            for (p in room.watchers) {
                tokens.add(p)
                Store.putPlayer(Player(p))
            }
            roomDestroyed = true
        } else {
            val index = room.players.indexOf(token)
            if (index >= 0) room.players[index] = ""
            val players = room.players.filter { s -> s.isNotEmpty() }
            tokens.addAll(players)
            tokens.addAll(room.watchers)
            roomDestroyed = false
        }
        if (roomDestroyed)
            Store.removeRoom(room.roomId)
        else
            Store.putRoom(room)
        Store.putPlayer(player)
        Store.notifyPlayerInfo(token, protoName) // 已经退出了，所以这里只能通知到自己
        // 需要再通知房间里的其他人
        var message: Message? = null
        for (t in tokens) {
            val conn = Supervisor.getChannel(t)
            if (conn != null) {
                if (roomDestroyed) {
                    conn.writeMessage(Message("room_info_sc", null, token, null))
                } else {
                    if (message == null)
                        message = Store.buildPlayerInfo(t)
                    conn.writeMessage(message)
                }
            }
        }
    }
}
