package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.*

object LinkTimeHandler : RequestHandler {
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val whose = m["whose"]!!.jsonPrimitive.int
        val event = m["event"]!!.jsonPrimitive.int
        whose == 0 || whose == 1 || throw HandlerException("参数错误")
        val room = player.room ?: throw HandlerException("不在房间里")
        room.isHost(player) || throw HandlerException("没有权限")
        room.type is RoomTypeLink || throw HandlerException("不支持这种操作")
        val linkData = room.linkData!!
        if (whose == 0) {
            when (event) {
                1 -> {
                    if (linkData.startMsA == 0L && linkData.endMsA == 0L) { // 开始
                        linkData.startMsA = System.currentTimeMillis()
                    } else if (linkData.startMsA > 0 && linkData.endMsA > 0) { // 继续
                        linkData.startMsA = System.currentTimeMillis() - (linkData.endMsA - linkData.startMsA)
                        linkData.endMsA = 0
                    } else {
                        throw HandlerException("已经在计时了，不能开始")
                    }
                }

                2, 3 -> {
                    if (linkData.startMsA > 0L && linkData.endMsA == 0L) { // 停止/暂停
                        linkData.endMsA = System.currentTimeMillis()
                    } else {
                        throw HandlerException("还未开始计时，不能停止")
                    }
                }

                else -> throw HandlerException("参数错误")
            }
            linkData.eventA = event
        } else {
            when (event) {
                1 -> {
                    if (linkData.startMsB == 0L && linkData.endMsB == 0L) { // 开始
                        linkData.startMsB = System.currentTimeMillis()
                    } else if (linkData.startMsB > 0 && linkData.endMsB > 0) { // 继续
                        linkData.startMsB = System.currentTimeMillis() - (linkData.endMsB - linkData.startMsA)
                        linkData.endMsB = 0
                    } else {
                        throw HandlerException("已经在计时了，不能开始")
                    }
                }

                2, 3 -> {
                    if (linkData.startMsB > 0L && linkData.endMsB == 0L) { // 停止/暂停
                        linkData.endMsB = System.currentTimeMillis()
                    } else {
                        throw HandlerException("还未开始计时，不能停止")
                    }
                }

                else -> throw HandlerException("参数错误")
            }
            linkData.eventB = event
        }
        room.push("push_link_data", linkData.encode())
        return null
    }
}
