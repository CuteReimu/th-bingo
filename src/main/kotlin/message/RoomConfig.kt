package org.tfcc.bingo.message

import org.tfcc.bingo.Room

class RoomConfig(
    val gameTime: Int?, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int?, // 倒计时，单位：秒
    val games: Array<String>?,
    val ranks: Array<String>?,
    val needWin: Int?,
    val difficulty: Int?,
    val isPrivate: Boolean?,
    val cdTime: Int?, // 选卡cd，收卡后要多少秒才能选下一张卡
    val reservedType: Int?, // 纯客户端用的一个类型字段
) {
    @Throws(HandlerException::class)
    fun validate(allowEmpty: Boolean) {
        if (!allowEmpty) {
            if (gameTime == null) throw HandlerException("游戏时长不能为空")
            if (countdown == null) throw HandlerException("倒计时不能为空")
            if (games == null) throw HandlerException("选择的作品不能为空")
            if (ranks == null) throw HandlerException("选择的难度不能为空")
            if (needWin == null) throw HandlerException("需要的胜场数不能为空")
            if (cdTime == null) throw HandlerException("选卡cd不能为空")
        }
        if (gameTime != null && gameTime <= 0) throw HandlerException("游戏时间必须大于0")
        if (gameTime != null && gameTime > 1440) throw HandlerException("游戏时间太长")
        if (countdown != null && countdown < 0) throw HandlerException("倒计时不能小于0")
        if (countdown != null && countdown > 86400) throw HandlerException("倒计时太长")
        if (games != null && games.size > 99) throw HandlerException("选择的作品数太多")
        if (ranks != null && ranks.size > 6) throw HandlerException("选择的难度数太多")
        if (needWin != null && needWin > 99) throw HandlerException("需要胜场的数值不正确")
        if (cdTime != null && cdTime < 0) throw HandlerException("选卡cd不能小于0")
        if (cdTime != null && cdTime > 1440) throw HandlerException("选卡cd太长")
    }

    fun updateRoom(room: Room) {
        gameTime?.let { room.gameTime = it }
        countdown?.let { room.countDown = it }
        games?.let { room.games = it }
        ranks?.let { room.ranks = it }
        needWin?.let { room.needWin = it.coerceAtLeast(1) }
        difficulty?.let { room.difficulty = it }
        isPrivate?.let { room.isPrivate = it }
        cdTime?.let { room.cdTime = it }
        reservedType?.let { room.reservedType = it }
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
            reservedType = room.reservedType,
        )
    }
}
