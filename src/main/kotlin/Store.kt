package org.tfcc.bingo

import com.jakewharton.disklrucache.DiskLruCache
import org.tfcc.bingo.message.HandlerException
import org.tfcc.bingo.message.Message
import org.tfcc.bingo.message.RoomInfoSc
import org.tfcc.bingo.message.writeMessage
import java.io.*
import java.util.*

object Store {
    private val cache: DiskLruCache

    init {
        val cacheDir = File("cache")
        if (cacheDir.exists() && !cacheDir.isDirectory) throw RuntimeException("初始化缓存失败")
        cacheDir.mkdirs()
        cache = DiskLruCache.open(cacheDir, 1, 1, 128 * 1024 * 1024)
    }

    @Throws(HandlerException::class)
    fun putPlayer(player: Player) {
        val editor = cache.edit("player-${player.token}") ?: throw HandlerException("缓存错误")
        try {
            val oos = ObjectOutputStream(editor.newOutputStream(0))
            oos.writeObject(player)
            oos.flush()
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    fun getPlayer(token: String): Player? {
        val entry = cache.get("player-$token") ?: return null
        return ObjectInputStream(entry.getInputStream(0)).readObject() as Player
    }

    fun putRoom(room: Room) {
        val editor = cache.edit("room-${room.roomId}") ?: throw HandlerException("缓存错误")
        try {
            val oos = ObjectOutputStream(editor.newOutputStream(0))
            oos.writeObject(room)
            oos.flush()
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    fun getRoom(roomId: String): Room? {
        val entry = cache.get("room-$roomId") ?: return null
        return ObjectInputStream(entry.getInputStream(0)).readObject() as Room
    }

    fun removeRoom(roomId: String) {
        if (!cache.remove(roomId)) throw HandlerException("缓存错误")
    }

    fun buildPlayerInfo(token: String): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc", null, null, null)
        player.roomId ?: return Message("room_info_sc", null, player.name, null)
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", null, player.name, null)
        val data = packRoomInfo(room) ?: return Message("room_info_sc", null, player.name, null)
        return Message(data)
    }

    private fun buildPlayerInfo(token: String, winnerIdx: Int): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc", null, null, null)
        player.roomId ?: return Message("room_info_sc", null, player.name, null)
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", null, player.name, null)
        val data = packRoomInfo(room, winnerIdx) ?: return Message("room_info_sc", null, player.name, null)
        return Message(data)
    }

    fun getAllPlayersInRoom(token: String): Array<String>? {
        val player = getPlayer(token) ?: return null
        if (player.roomId.isNullOrEmpty()) return arrayOf(token)
        val room = getRoom(player.roomId) ?: return arrayOf(token)
        val l = ArrayList<String>()
        if (room.host.isNotEmpty()) l.add(room.host)
        for (token1 in room.players ?: return arrayOf(room.host)) {
            if (token1.isNotEmpty() && !l.contains(token1)) l.add(token1)
        }
        return l.toArray(arrayOf<String>())
    }

    fun notifyPlayerInfo(token: String, reply: String?) {
        val message = buildPlayerInfo(token)
        for (token1 in getAllPlayersInRoom(token) ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(
                if (token1 != token) message else Message(
                    message.name,
                    reply,
                    message.trigger,
                    message.data
                )
            )
        }
    }

    fun notifyPlayerInfo(token: String, reply: String?, winnerIdx: Int) {
        val message = buildPlayerInfo(token, winnerIdx)
        for (token1 in getAllPlayersInRoom(token) ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(
                if (token1 != token) message else Message(
                    message.name,
                    reply,
                    message.trigger,
                    message.data
                )
            )
        }
    }

    fun notifyPlayersInRoom(token: String, reply: String?, message: Message) {
        val tokens = getAllPlayersInRoom(token)
        val trigger = getPlayer(token)?.name
        for (token1 in tokens ?: return) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(
                if (token1 != token) message else Message(
                    message.name,
                    reply,
                    trigger,
                    message.data
                )
            )
        }
    }

    private fun packRoomInfo(room: Room): RoomInfoSc? {
        val host = getPlayer(room.host)?.name ?: return null
        val players = Array(room.players!!.size) { i ->
            getPlayer(room.players!![i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = null
        )
    }

    private fun packRoomInfo(room: Room, winnerIdx: Int): RoomInfoSc? {
        val host = getPlayer(room.host)?.name ?: return null
        val players = Array(room.players!!.size) { i ->
            getPlayer(room.players!![i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = winnerIdx
        )
    }
}