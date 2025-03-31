package org.tfcc.bingo

import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.message.*
import java.io.*
import java.util.*

object Store {
    const val ROBOT_NAME = "训练用毛玉"
    private val playerCache = HashMap<String, Player>()
    private val roomCache = HashMap<String, Room>()
    fun newRobotPlayer() = Player(pwd = ROBOT_NAME, name = ROBOT_NAME)

    init {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                Dispatcher.pool.submit(::clean)
            }
        }, 500, 30 * 60 * 1000)
    }

    private fun clean() {
        logger.debug("开始清除过期缓存")
        val now = System.currentTimeMillis()
        val outdatedPlayers = HashSet<String>()
        val outdatedRooms = HashSet<String>()
        for ((playerName, player) in playerCache) {
            player.room == null || continue // 只清除无房间的玩家
            now - player.lastOperateMs >= 2 * 3600 * 1000 || continue // 只清除2小时以上无操作的玩家
            outdatedPlayers.add(playerName)
        }
        for ((roomId, room) in roomCache) {
            now - room.lastOperateMs >= 6 * 3600 * 1000 || continue // 只清除6小时以上无操作的房间
            outdatedRooms.add(roomId)
            room.host?.let { outdatedPlayers.add(it.name) }
            room.players.forEach { p -> p?.let { outdatedPlayers.add(it.name) } }
            room.watchers.forEach { outdatedPlayers.add(it.name) }
        }
        for (playerName in outdatedPlayers) {
            logger.info("玩家 $playerName 过期, 自动清除")
            playerCache.remove(playerName)
        }
        for (roomId in outdatedRooms) {
            logger.info("房间 $roomId 过期, 自动清除")
            roomCache.remove(roomId)
        }
    }

    @Throws(HandlerException::class)
    fun getPlayer(name: String, pwd: String? = null): Player {
        name != ROBOT_NAME || throw HandlerException("不能使用这个名字")
        var player = playerCache[name]
        if (player != null) {
            pwd == null || player.pwd == pwd || throw HandlerException("密码错误")
            return player
        }
        player = Player(name, pwd!!)
        playerCache[name] = player
        return player
    }

    fun putRoom(room: Room) {
        roomCache[room.roomId] = room
    }

    fun getRoom(roomId: String): Room? {
        return roomCache[roomId]
    }

    fun removeRoom(roomId: String) {
        roomCache.remove(roomId)
    }
}
