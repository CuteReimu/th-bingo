package org.tfcc.bingo.message

import org.tfcc.bingo.Room

class RoomConfig(
    val gameTime: Int, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int, // 倒计时，单位：秒
    val games: Array<String>,
    val ranks: Array<String>?,
    val needWin: Int,
    val difficulty: Int,
    val isPrivate: Boolean?,
    val cdTime: Int = 30, // 选卡cd，收卡后要多少秒才能选下一张卡
) {
    @Throws(HandlerException::class)
    fun validate() {
        if (gameTime <= 0) throw HandlerException("游戏时间必须大于0")
        if (gameTime > 1440) throw HandlerException("游戏时间太长")
        if (countdown < 0) throw HandlerException("倒计时不能小于0")
        if (countdown > 86400) throw HandlerException("倒计时太长")
        if (games.size > 99) throw HandlerException("选择的作品数太多")
        if (ranks != null && ranks.size > 6) throw HandlerException("选择的难度数太多")
        if (needWin > 99) throw HandlerException("需要胜场的数值不正确")
        if (cdTime < 0) throw HandlerException("选卡cd不能小于0")
        if (cdTime > 1440) throw HandlerException("选卡cd太长")
    }

    fun updateRoom(room: Room) {
        room.gameTime = gameTime
        room.countDown = countdown
        room.games = games
        room.ranks = ranks
        room.needWin = needWin.coerceAtLeast(1)
        room.difficulty = difficulty
        room.isPrivate = isPrivate == true
        room.cdTime = cdTime
    }

    companion object {
        fun fromRoom(room: Room) = RoomConfig(
            gameTime = room.gameTime,
            countdown = room.countDown,
            games = room.games,
            ranks = room.ranks,
            needWin = room.needWin,
            difficulty = room.difficulty,
            isPrivate = room.isPrivate,
            cdTime = room.cdTime,
        )
    }
}