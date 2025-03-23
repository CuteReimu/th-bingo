package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.tfcc.bingo.*
import java.util.*

object GetAllSpellsHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement {
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还未开始")
        val playerIndex = room.players.indexOf(player)
        var leftTime = 0L
        var status = 0
        var leftCdTime = 0L
        var now = System.currentTimeMillis()
        val countdown = room.roomConfig.countdown * 1000L
        val gameTime = room.roomConfig.gameTime * 60000L
        val cdTime = (room.roomConfig.cdTime ?: 0) * 1000L
        if (room.started) {
            if (now < room.startMs + countdown) {
                status = 1 // 倒计时
                leftTime = room.startMs + countdown - now
            } else if (now < room.startMs + countdown + gameTime + room.totalPauseMs) {
                status = 2 // 进行中
                if (room.pauseBeginMs > 0) {
                    status = 3 // 暂停中
                    now = room.pauseBeginMs // 暂停中，可以把开始暂停的时间视为当前时间，方便下文计算
                }
                leftTime = room.startMs + countdown + gameTime + room.totalPauseMs - now
                if (playerIndex >= 0)
                    leftCdTime = cdTime - (now - room.lastGetTime[playerIndex])
            } else {
                status = 4 // 已结束
            }
        }
        return AllSpellsResponse(
            spells = room.spells!!,
            spellStatus = room.spellStatus!!.map { it.value },
            leftTime = leftTime,
            status = status,
            leftCdTime = leftCdTime,
        ).encode()
    }
}

@Serializable
class AllSpellsResponse(
    /** 25张符卡 */
    val spells: Array<Spell>,
    /** 25张符卡的收取状态 */
    @SerialName("spell_status")
    val spellStatus: List<Int>,
    /** 倒计时剩余时间，单位：毫秒 */
    @SerialName("left_time")
    val leftTime: Long,
    /** 0-未开始，1-赛前倒计时中，2-开始，3-暂停中，4-结束 */
    val status: Int,
    /** 选卡cd剩余时间，单位：毫秒 */
    @SerialName("left_cd_time")
    val leftCdTime: Long,
)
