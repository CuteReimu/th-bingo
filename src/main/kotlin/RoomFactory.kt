package org.tfcc.bingo

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import org.apache.log4j.Logger
import java.util.concurrent.ConcurrentHashMap

@OptIn(DelicateCoroutinesApi::class)
class RoomActor private constructor() {
    private val channel = Channel<Runnable>(Channel.UNLIMITED)

    init {
        GlobalScope.launch {
            while (true) try {
                val callback = channel.receive()
                callback.run()
            } catch (_: ClosedReceiveChannelException) {
                break
            } catch (e: Exception) {
                logger.error("error occur: ", e)
            }
        }
    }

    fun post(callback: Runnable) {
        channel.trySend(callback)
    }

    private fun stop() {
        channel.close()
    }

    companion object {
        private val logger: Logger = Logger.getLogger(RoomActor::class.java)
        private val cache = ConcurrentHashMap<String, RoomActor>()

        operator fun get(roomId: String): RoomActor {
            return cache.getOrPut(roomId) { RoomActor() }
        }

        fun stop(roomId: String) {
            cache[roomId]?.stop()
        }
    }
}