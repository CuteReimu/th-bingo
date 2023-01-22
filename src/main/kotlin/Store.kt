package org.tfcc.bingo

import org.apache.log4j.Logger
import java.io.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Store {
    private val logger = Logger.getLogger(this.javaClass)
    private const val FILE_NAME = "store.dat"
    private val pool = Executors.newSingleThreadExecutor()
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
        } finally {
            file?.close()
        }
    }

    fun putPlayer(p: Player) {
        val player = p.copy()
        pool.submit {
            playerCache[player.token] = player
            var file: ObjectOutputStream? = null
            try {
                file = ObjectOutputStream(FileOutputStream(FILE_NAME))
                file.writeObject(playerCache)
                file.writeObject(roomCache)
            } catch (e: IOException) {
                logger.error(e)
            } finally {
                file?.close()
            }
        }
    }

    fun getPlayer(token: String): Player? {
        return pool.submit(Callable { playerCache[token] }).get()
    }

    fun putRoom(r: Room) {
        val room = r.copy()
        pool.submit {
            roomCache[room.roomId] = room
            var file: ObjectOutputStream? = null
            try {
                file = ObjectOutputStream(FileOutputStream(FILE_NAME))
                file.writeObject(playerCache)
                file.writeObject(roomCache)
            } catch (e: IOException) {
                logger.error(e)
            } finally {
                file?.close()
            }
        }
    }

    fun getRoom(roomId: String): Room? {
        return pool.submit(Callable { roomCache[roomId] }).get()
    }

//    fun buildPlayerInfo(token: String) :
}