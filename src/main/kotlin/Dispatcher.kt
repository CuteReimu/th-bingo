package org.tfcc.bingo

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import kotlinx.serialization.json.*
import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.message.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private fun ChannelHandlerContext.writeMessage(message: ResponseMessage): ChannelFuture {
    val text = Dispatcher.json.encodeToString(message)
    return writeAndFlush(TextWebSocketFrame(text))
}

fun Player.push(pushAction: String, pushData: JsonElement?, mustOnline: Boolean = false): ChannelFuture? {
    if (name == Store.robotName) {
        if (mustOnline) throw HandlerException("对方已离线")
        else return null
    }
    val channel = Supervisor.getChannel(name)
    if (channel == null && mustOnline)
        throw HandlerException("对方已离线")
    val text = Dispatcher.json.encodeToString(PushMessage(pushAction, pushData))
    return channel?.writeAndFlush(TextWebSocketFrame(text))
}

fun Room.push(pushAction: String, pushData: JsonElement?) {
    host?.push(pushAction, pushData)
    players.forEach { it?.push(pushAction, pushData) }
    watchers.forEach { it.push(pushAction, pushData) }
}

private val handlers = mapOf(
    "login" to LoginHandler,
    "heart" to HeartHandler,
    "create_room" to CreateRoomHandler,
    "get_room_config" to GetRoomConfigHandler,
    "update_room_config" to UpdateRoomConfigHandler,
    "join_room" to JoinRoomHandler,
    "get_room" to GetRoomHandler,
    "leave_room" to LeaveRoomHandler,
    "stand_up" to StandUpHandler,
    "sit_down" to SitDownHandler,
    "set_phase" to SetPhaseHandler,
    "get_phase" to GetPhaseHandler,
    "start_game" to StartGameHandler,
    "stop_game" to StopGameHandler,
    "gm_warn_player" to GmWarnPlayerHandler,
    "update_change_card_count" to UpdateChangeCardCountHandler,
    "pause" to PauseHandler,
    "reset_room" to ResetRoomHandler,
    "set_debug_spells" to SetDebugSpellsHandler,
    "get_all_spells" to GetAllSpellsHandler,
    "select_spell" to SelectSpellHandler,
    "finish_spell" to FinishSpellHandler,
    "update_spell_status" to UpdateSpellStatusHandler,
    "ban_pick" to BanPickHandler,
    "start_ban_pick" to StartBanPickHandler,
)

inline fun <reified T> JsonElement.decode(): T = Dispatcher.json.decodeFromJsonElement(this)

inline fun <reified T> T.encode(): JsonElement = Dispatcher.json.encodeToJsonElement(this)

object Dispatcher {
    val pool: ExecutorService = Executors.newSingleThreadExecutor()

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        allowStructuredMapKeys = true
    }

    fun handle(ctx: ChannelHandlerContext, text: String) {
        var echo: JsonElement? = null
        try {
            val m = json.parseToJsonElement(text).jsonObject
            echo = m["echo"]
            val action = m["action"]!!.jsonPrimitive.content
            if (action != "heart")
                logger.debug("收到${ctx.channel().id().asShortText()}：$text")
            val data = m["data"]
            pool.submit {
                try {
                    val player: Player
                    val playerName = Supervisor.getPlayerName(ctx.channel())
                    if (action == "login") {
                        if (playerName != null) {
                            ctx.writeMessage(ResponseMessage(-1, "You have already logged", null, echo))
                            return@submit
                        }
                        val dataObj = data!!.jsonObject
                        val name = dataObj["name"]!!.jsonPrimitive.content
                        name.toByteArray().size <= 48 || throw HandlerException("名字太长")
                        val pwd = dataObj["pwd"]!!.jsonPrimitive.content
                        player = Store.getPlayer(name, pwd)
                        Supervisor.put(ctx.channel(), player.name)
                    } else {
                        if (playerName == null) {
                            ctx.writeMessage(ResponseMessage(-1, "You haven't login", null, echo))
                            return@submit
                        }
                        player = Store.getPlayer(playerName)
                    }
                    val handler = handlers[action]!!
                    val now = System.currentTimeMillis()
                    player.lastOperateMs = now
                    player.room?.lastOperateMs = now // 对于离开房间类协议，在执行之前需要修改
                    val response = handler.handle(ctx, player, data)
                    player.room?.lastOperateMs = now // 对于加入房间类协议，在执行之后需要修改
                    Dispatcher.logger.debug("返回${ctx.channel().id().asShortText()}：$text")
                    ctx.writeMessage(ResponseMessage(0, "ok", response, echo))
                } catch (e: IllegalArgumentException) {
                    logger.error("illegal json", e)
                    ctx.writeMessage(ResponseMessage(400, "illegal json", null, echo))
                } catch (e: NullPointerException) {
                    logger.error("illegal request", e)
                    ctx.writeMessage(ResponseMessage(400, "illegal request", null, echo))
                } catch (e: HandlerException) {
                    logger.warn("handle failed: $action", e)
                    ctx.writeMessage(ResponseMessage(500, e.message ?: "handle failed", null, echo))
                } catch (e: Throwable) {
                    logger.error("handler unknown throwable: $action", e)
                    ctx.writeMessage(ResponseMessage(500, "handler unknown throwable", null, echo))
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.error("illegal json", e)
            ctx.writeMessage(ResponseMessage(400, "illegal json", null, echo))
        } catch (e: NullPointerException) {
            logger.error("illegal request", e)
            ctx.writeMessage(ResponseMessage(400, "illegal request", null, echo))
        } catch (e: Throwable) {
            logger.error("server error", e)
            ctx.writeMessage(ResponseMessage(500, "server error", null, echo))
        }
    }
}
