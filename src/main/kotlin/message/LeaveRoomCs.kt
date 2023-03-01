package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.*

class LeaveRoomCs : Handler {
    override fun handle(ctx: ChannelHandlerContext, player: Player?, room: Room?, protoName: String) {
        if (player == null) throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        if (room == null) throw HandlerException("找不到房间")
        if ((room.host == player.token || room.players.contains(player.token)) && room.started) throw HandlerException("比赛已经开始了，不能退出")
        if (room.players.contains(player.token) && room.locked) throw HandlerException("连续比赛没结束，不能退出")
        val tokens = ArrayList<String>()
        val roomDestroyed: Boolean
        var isWatcher = false
        if (room.host == player.token) {
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
            val index = room.players.indexOf(player.token)
            if (index >= 0) {
                room.players[index] = ""
            } else {
                isWatcher = true
                room.watchers.remove(player.token)
            }
            val players = room.players.filter { s -> s.isNotEmpty() }
            if (room.host.isNotEmpty()) tokens.add(room.host)
            tokens.addAll(players)
            tokens.addAll(room.watchers)
            roomDestroyed = room.host.isEmpty() && players.isEmpty()
        }
        if (roomDestroyed) {
            Store.removeRoom(room.roomId)
            RoomActor.stop(room.roomId)
        } else {
            Store.putRoom(room, !isWatcher)
        }
        Store.putPlayer(Player(token = player.token, roomId = "", name = player.name))
        Store.notifyPlayerInfo(player.token, protoName) // 已经退出了，所以这里只能通知到自己
        // 需要再通知房间里的其他人
        var message: Message? = null
        for (t in tokens) {
            val conn = Supervisor.getChannel(t)
            if (conn != null) {
                if (roomDestroyed) {
                    conn.writeMessage(Message("room_info_sc", null, player.token, null))
                } else {
                    if (message == null)
                        message = Store.buildPlayerInfo(t)
                    conn.writeMessage(message)
                }
            }
        }
    }
}
