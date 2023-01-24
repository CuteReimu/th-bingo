package org.tfcc.bingo.message

import io.netty.channel.ChannelHandlerContext
import org.tfcc.bingo.SpellStatus
import org.tfcc.bingo.Store
import java.util.*

data class StartGameCs(
    val gameTime: UInt, // 游戏总时间（不含倒计时），单位：分
    val countdown: UInt, // 倒计时，单位：秒
    val games: Array<String>,
    val ranks: Array<String>,
    var needWin: UInt
) : Handler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, token: String, protoName: String) {
        if (gameTime == 0U) throw HandlerException("游戏时间不能为0")
        if (gameTime > 1440U) throw HandlerException("游戏时间太长")
        if (countdown > 86400U) throw HandlerException("倒计时太长")
        if (games.size > 99) throw HandlerException("选择的作品数太多")
        if (ranks.size > 6) throw HandlerException("选择的难度数太多")
        if (needWin > 99U) throw HandlerException("需要胜场的数值不正确")
        if (needWin == 0U) needWin = 1U
        val player = Store.getPlayer(token) ?: throw HandlerException("找不到玩家")
        if (player.roomId.isNullOrEmpty()) throw HandlerException("不在房间里")
        val room = Store.getRoom(player.roomId) ?: throw HandlerException("找不到房间")
        if (room.host != token)
            throw HandlerException("你不是房主")
        else if (!room.started)
            throw HandlerException("游戏已经开始")
        else if (!room.players!!.contains(""))
            throw HandlerException("玩家没满")
        room.started = true
        room.spells = room.type.randSpells(games, ranks)
        room.startMs = Date().time
        room.countDown = countdown
        room.gameTime = gameTime
        room.spellStatus = Array(room.spells!!.size) { _ -> SpellStatus.NONE }
        room.needWin = needWin
        room.locked = true
        room.type.onStart(room)
        Store.putRoom(room)
        Store.notifyPlayersInRoom(
            token,
            protoName,
            Message(
                reply = null,
                trigger = player.name,
                data = SpellListSc(
                    spells = room.spells!!,
                    time = room.startMs,
                    startTime = room.startMs,
                    gameTime = gameTime,
                    countdown = countdown,
                    needWin = needWin,
                    whoseTurn = room.bpData!!.whoseTurn,
                    banPick = room.bpData!!.banPick,
                    linkData = room.linkData,
                    phase = room.phase,
                    pauseBeginMs = 0L,
                    status = null,
                    totalPauseTime = 0L
                )
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StartGameCs

        if (gameTime != other.gameTime) return false
        if (countdown != other.countdown) return false
        if (!games.contentEquals(other.games)) return false
        if (!ranks.contentEquals(other.ranks)) return false
        if (needWin != other.needWin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameTime.hashCode()
        result = 31 * result + countdown.hashCode()
        result = 31 * result + games.contentHashCode()
        result = 31 * result + ranks.contentHashCode()
        result = 31 * result + needWin.hashCode()
        return result
    }
}
