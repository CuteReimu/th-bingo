package org.tfcc.bingo

import com.google.gson.Gson
import com.jakewharton.disklrucache.DiskLruCache
import org.apache.log4j.Logger
import org.tfcc.bingo.message.*
import java.io.*
import java.util.*

object Store {
    private val logger = Logger.getLogger(Store.javaClass)
    private val cache: DiskLruCache
    private val gson = Gson()
    val robotPlayer = Player(token = "训练用毛玉", name = "训练用毛玉", roomId = null)

    init {
        val cacheDir = File("cache")
        if (cacheDir.exists() && !cacheDir.isDirectory) throw RuntimeException("初始化缓存失败")
        cacheDir.mkdirs()
        cache = DiskLruCache.open(cacheDir, 1, 1, 10 * 1024 * 1024)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                Dispatcher.pool.submit(::clean)
            }
        }, 500, 30 * 60 * 1000)
    }

    private fun clean() {
        logger.debug("开始清除过期缓存")
        val now = System.currentTimeMillis()
        val rooms = File("cache").listFiles { _, name -> name.startsWith("room-") && name.endsWith(".0") }
        if (rooms != null) {
            for (f in rooms) {
                val name = f.name
                val room = getRoom(name.substring("room-".length, name.length - ".0".length)) ?: continue
                if (now >= room.lastOperateMs + 2 * 60 * 60 * 1000) {
                    logger.info("房间 ${room.roomId} 过期, 自动清除")
                    removeRoom(room.roomId)
                }
            }
        }
        val players = File("cache").listFiles { _, name -> name.startsWith("player-") && name.endsWith(".0") }
        if (players != null) {
            for (f in players) {
                val name = f.name
                val player = getPlayer(name.substring("player-".length, name.length - ".0".length)) ?: continue
                if (now >= player.lastOperateMs + 6 * 60 * 60 * 1000 && (player.roomId == null || getRoom(player.roomId) == null)) {
                    logger.info("玩家 ${player.token} 过期, 自动清除")
                    removePlayer(player.token)
                }
            }
        }
    }

    private val isWindows = System.getProperty("os.name").lowercase().contains("windows")

    @Throws(HandlerException::class)
    fun putPlayer(player: Player) {
        if (player.token == robotPlayer.token) return
        player.lastOperateMs = System.currentTimeMillis()
        if (isWindows)
            cache.remove("player-${player.token}")
        val editor = cache.edit("player-${player.token}") ?: throw HandlerException("缓存错误")
        try {
            editor.newOutputStream(0).use { os ->
                BufferedWriter(OutputStreamWriter(os, Charsets.UTF_8)).use { gson.toJson(player, it) }
            }
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    fun getPlayer(token: String): Player? {
        if (token == robotPlayer.token) return robotPlayer
        val entry = cache.get("player-$token") ?: return null
        entry.getInputStream(0).use { `is` ->
            BufferedReader(InputStreamReader(`is`, Charsets.UTF_8)).use { return gson.fromJson(it, Player::class.java) }
        }
    }

    private fun removePlayer(token: String) {
        if (token == robotPlayer.token) return
        cache.remove("player-$token")
    }

    @Throws(HandlerException::class)
    fun putRoom(room: Room, needUpdateOperateMs: Boolean = true) {
        if (needUpdateOperateMs) room.lastOperateMs = System.currentTimeMillis()
        if (isWindows)
            cache.remove("room-${room.roomId}")
        val editor = cache.edit("room-${room.roomId}") ?: throw HandlerException("缓存错误")
        try {
            editor.newOutputStream(0).use { os ->
                BufferedWriter(OutputStreamWriter(os, Charsets.UTF_8)).use { gson.toJson(room, it) }
            }
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    fun getRoom(roomId: String): Room? {
        val entry = cache.get("room-$roomId") ?: return null
        entry.getInputStream(0).use { `is` ->
            BufferedReader(InputStreamReader(`is`, Charsets.UTF_8)).use { return gson.fromJson(it, Room::class.java) }
        }
    }

    fun removeRoom(roomId: String) {
        cache.remove("room-$roomId")
    }

    fun buildPlayerInfo(token: String): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc")
        player.roomId ?: return Message("room_info_sc")
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", trigger = player.name)
        val data = packRoomInfo(room)
        return Message(data = data)
    }

    private fun buildPlayerInfo(token: String, winnerIdx: Int): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc")
        player.roomId ?: return Message("room_info_sc", trigger = player.name)
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", trigger = player.name)
        val data = packRoomInfo(room, winnerIdx)
        return Message(data = data)
    }

    fun getAllPlayersInRoom(token: String): Array<String>? {
        val player = getPlayer(token) ?: return null
        if (player.roomId.isNullOrEmpty()) return arrayOf(token)
        val room = getRoom(player.roomId) ?: return arrayOf(token)
        val l = ArrayList<String>()
        if (room.host.isNotEmpty()) l.add(room.host)
        for (token1 in room.players) {
            if (token1.isNotEmpty() && !l.contains(token1)) l.add(token1)
        }
        for (token1 in room.watchers) {
            if (token1.isNotEmpty() && !l.contains(token1)) l.add(token1)
        }
        return l.toArray(arrayOf<String>())
    }

    fun notifyPlayerInfo(token: String, reply: String?) {
        val message = buildPlayerInfo(token)
        for (token1 in getAllPlayersInRoom(token) ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(if (token1 != token) message else message.copy(reply = reply))
        }
    }

    fun notifyPlayerInfo(token: String, reply: String?, winnerIdx: Int) {
        val message = buildPlayerInfo(token, winnerIdx)
        for (token1 in getAllPlayersInRoom(token) ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(if (token1 != token) message else message.copy(reply = reply))
        }
    }

    fun notifyPlayersInRoom(token: String, reply: String?, message: Message) {
        val tokens = getAllPlayersInRoom(token)
        val trigger = getPlayer(token)?.name
        for (token1 in tokens ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(if (token1 != token) message else message.copy(reply = reply, trigger = trigger))
        }
    }

    private fun packRoomInfo(room: Room): RoomInfoSc {
        val host = getPlayer(room.host)?.name ?: ""
        val players = Array(room.players.size) { i ->
            getPlayer(room.players[i])?.name ?: ""
        }
        val watchers = Array(room.watchers.size) { i ->
            getPlayer(room.watchers[i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = null,
            watchers = watchers,
            roomConfig = RoomConfig.fromRoom(room),
        )
    }

    private fun packRoomInfo(room: Room, winnerIdx: Int): RoomInfoSc {
        val host = getPlayer(room.host)?.name ?: ""
        val players = Array(room.players.size) { i ->
            getPlayer(room.players[i])?.name ?: ""
        }
        val watchers = Array(room.watchers.size) { i ->
            getPlayer(room.watchers[i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = winnerIdx,
            watchers = watchers,
            roomConfig = RoomConfig.fromRoom(room),
        )
    }
}