package org.tfcc.bingo

import org.apache.log4j.Logger
import org.tfcc.bingo.message.Message
import org.tfcc.bingo.message.RoomInfoSc
import org.tfcc.bingo.message.writeMessage
import java.io.*

object Store {
    private val logger = Logger.getLogger(this.javaClass)
    private const val FILE_NAME = "store.dat"
    private val playerCache = HashMap<String, Player>()
    private val roomCache = HashMap<String, Room>()

    init {
        var file: ObjectInputStream? = null
        try {
            file = ObjectInputStream(FileInputStream(FILE_NAME))
            @Suppress("UNCHECKED_CAST")
            playerCache.putAll(file.readObject() as HashMap<String, Player>)
            @Suppress("UNCHECKED_CAST")
            roomCache.putAll(file.readObject() as HashMap<String, Room>)
        } catch (_: FileNotFoundException) {
            // Ignored
        } finally {
            file?.close()
        }
    }

    fun putPlayer(player: Player) { // Player 的成员变量全是 val ，因此不需要 copy()
        playerCache[player.token] = player
        var file: ObjectOutputStream? = null
        try {
            file = ObjectOutputStream(BufferedOutputStream(FileOutputStream(FILE_NAME)))
            file.writeObject(playerCache)
            file.writeObject(roomCache)
        } catch (e: IOException) {
            logger.error(e)
        } finally {
            file?.close()
        }
    }

    fun getPlayer(token: String): Player? {
        return playerCache[token]
    }

    fun putRoom(room: Room) {
        roomCache[room.roomId] = room.copy()
        var file: ObjectOutputStream? = null
        try {
            file = ObjectOutputStream(BufferedOutputStream(FileOutputStream(FILE_NAME)))
            file.writeObject(playerCache)
            file.writeObject(roomCache)
        } catch (e: IOException) {
            logger.error(e)
        } finally {
            file?.close()
        }
    }

    fun getRoom(roomId: String): Room? {
        return roomCache[roomId]
    }

    fun removeRoom(roomId: String) {
        roomCache.remove(roomId)
        var file: ObjectOutputStream? = null
        try {
            file = ObjectOutputStream(BufferedOutputStream(FileOutputStream(FILE_NAME)))
            file.writeObject(playerCache)
            file.writeObject(roomCache)
        } catch (e: IOException) {
            logger.error(e)
        } finally {
            file?.close()
        }
    }

    fun buildPlayerInfo(token: String): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc", null, null, null)
        player.roomId ?: return Message("room_info_sc", null, player.name, null)
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", null, player.name, null)
        val data = packRoomInfo(room) ?: return Message("room_info_sc", null, player.name, null)
        return Message(data)
    }

    fun buildPlayerInfo(token: String, winnerIdx: Int): Message {
        val player = getPlayer(token) ?: return Message("room_info_sc", null, null, null)
        player.roomId ?: return Message("room_info_sc", null, player.name, null)
        val room = getRoom(player.roomId) ?: return Message("room_info_sc", null, player.name, null)
        val data = packRoomInfo(room, winnerIdx) ?: return Message("room_info_sc", null, player.name, null)
        return Message(data)
    }

    fun getAllPlayersInRoom(token: String): Array<String>? {
        val player = getPlayer(token) ?: return null
        if (player.roomId.isNullOrEmpty()) return null
        val room = getRoom(player.roomId) ?: return null
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

    fun packRoomInfo(room: Room): RoomInfoSc? {
        getPlayer(room.host) ?: return null
        val players = Array(room.players!!.size) { i ->
            getPlayer(room.players!![i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = room.host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = null
        )
    }

    fun packRoomInfo(room: Room, winnerIdx: Int): RoomInfoSc? {
        getPlayer(room.host) ?: return null
        val players = Array(room.players!!.size) { i ->
            getPlayer(room.players!![i])?.name ?: ""
        }
        return RoomInfoSc(
            rid = room.roomId,
            type = room.roomType,
            host = room.host,
            names = players,
            changeCardCount = room.changeCardCount,
            started = room.started,
            score = room.score,
            winner = winnerIdx
        )
    }
}