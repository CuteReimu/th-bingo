package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.RoomTypeLink
import org.tfcc.bingo.Store

class LinkTimeCs(val whose: Int, val event: Int) : Handler {
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (whose != 0 && whose != 1) throw HandlerException("参数错误")
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (!room.isHost(token)) throw HandlerException("没有权限")
        if (room.type != RoomTypeLink) throw HandlerException("不支持这种操作")
        val data = room.linkData!!
        if (whose == 0) {
            when (event) {
                1 -> {
                    if (data.startMsA == 0L && data.endMsA == 0L) { // 开始
                        data.startMsA = System.currentTimeMillis()
                    } else if (data.startMsA > 0 && data.endMsA > 0) { // 继续
                        data.startMsA = System.currentTimeMillis() - (data.endMsA - data.startMsA)
                        data.endMsA = 0
                    } else {
                        throw HandlerException("已经在计时了，不能开始")
                    }
                }

                2, 3 -> {
                    if (data.startMsA > 0L && data.endMsA == 0L) { // 停止/暂停
                        data.endMsA = System.currentTimeMillis()
                    } else {
                        throw HandlerException("还未开始计时，不能停止")
                    }
                }

                else -> throw HandlerException("参数错误")
            }
            data.eventA = event
        } else {
            when (event) {
                1 -> {
                    if (data.startMsB == 0L && data.endMsB == 0L) { // 开始
                        data.startMsB = System.currentTimeMillis()
                    } else if (data.startMsB > 0 && data.endMsB > 0) { // 继续
                        data.startMsB = System.currentTimeMillis() - (data.endMsB - data.startMsA)
                        data.endMsB = 0
                    } else {
                        throw HandlerException("已经在计时了，不能开始")
                    }
                }

                2, 3 -> {
                    if (data.startMsB > 0L && data.endMsB == 0L) { // 停止/暂停
                        data.endMsB = System.currentTimeMillis()
                    } else {
                        throw HandlerException("还未开始计时，不能停止")
                    }
                }

                else -> throw HandlerException("参数错误")
            }
            data.eventB = event
        }
        Store.putRoom(room)
        Store.notifyPlayersInRoom(token, protoName, Message(data = LinkDataSc(data)))
    }
}
