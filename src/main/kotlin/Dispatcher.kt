package org.tfcc.bingo

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import kotlinx.serialization.json.*
import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.message.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private fun ChannelHandlerContext.writeMessage(message: ResponseMessage, writeLog: Boolean = true): ChannelFuture {
    val text = Dispatcher.json.encodeToString(message)
    if (writeLog) Dispatcher.logger.debug("返回${channel().id().asShortText()}：$text")
    return writeAndFlush(TextWebSocketFrame(text))
}

private fun Channel.push(pushAction: String, pushData: JsonElement?): ChannelFuture {
    val text = Dispatcher.json.encodeToString(PushMessage(pushAction, pushData))
    Dispatcher.logger.debug("推送${id().asShortText()}：$text")
    return writeAndFlush(TextWebSocketFrame(text))
}

fun Player.push(pushAction: String, pushData: JsonElement?, mustOnline: Boolean = false): ChannelFuture? {
    if (name == Store.robotName) {
        if (mustOnline) throw HandlerException("对方已离线")
        else return null
    }
    val channel = Supervisor.getChannel(name)
    if (channel == null) {
        if (mustOnline) throw HandlerException("对方已离线")
        Dispatcher.logger.warn("对方已离线：$name，推送失败")
        return null
    }
    val text = Dispatcher.json.encodeToString(PushMessage(pushAction, pushData))
    Dispatcher.logger.debug("推送${channel.id().asShortText()}：$text")
    return channel.writeAndFlush(TextWebSocketFrame(text))
}

fun Room.push(pushAction: String, pushData: JsonElement?) {
    host?.push(pushAction, pushData)
    players.forEach { it?.push(pushAction, pushData) }
    watchers.forEach { it.push(pushAction, pushData) }
}

private val handlers = mapOf(
    "login" to LoginHandler,
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
    "cancel_select_spell" to CancelSelectSpellHandler,
    "finish_spell" to FinishSpellHandler,
    "update_spell_status" to UpdateSpellStatusHandler,
    "ban_pick" to BanPickHandler,
    "start_ban_pick" to StartBanPickHandler,
    "bp_game_ban_pick" to BpGameBanPickHandler,
    "bp_game_next_round" to BpGameNextRoundHandler,
    "link_time" to LinkTimeHandler,
)

inline fun <reified T> JsonElement.decode(): T = Dispatcher.json.decodeFromJsonElement(this)

inline fun <reified T> T.encode(): JsonElement = Dispatcher.json.encodeToJsonElement(this)

object Dispatcher {
    val pool: ExecutorService = Executors.newSingleThreadExecutor()

    val json = Json {
        ignoreUnknownKeys = true
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
                    when (action) {
                        "login" -> {
                            if (playerName != null) {
                                ctx.writeMessage(ResponseMessage(-1, "You have already logged", null, echo))
                                return@submit
                            }
                            val dataObj = data!!.jsonObject
                            val name = dataObj["name"]!!.jsonPrimitive.content
                            name.toByteArray().size <= 48 || throw HandlerException("名字太长")
                            val pwd = dataObj["pwd"]!!.jsonPrimitive.content
                            player = Store.getPlayer(name, pwd)
                            val oldChannel = Supervisor.put(ctx.channel(), player.name)
                            oldChannel?.push("push_kick", null)
                        }

                        "heart" -> {
                            val response = JsonObject(mapOf("now" to JsonPrimitive(System.currentTimeMillis())))
                            ctx.writeMessage(ResponseMessage(0, "ok", response, echo), false)
                            return@submit
                        }

                        else -> {
                            if (playerName == null) {
                                ctx.writeMessage(ResponseMessage(-1, "You haven't login", null, echo))
                                return@submit
                            }
                            player = Store.getPlayer(playerName)
                        }
                    }
                    val handler = handlers[action] ?: throw HandlerException("unknown action")
                    val now = System.currentTimeMillis()
                    player.lastOperateMs = now
                    player.room?.lastOperateMs = now // 对于离开房间类协议，在执行之前需要修改
                    val response = handler.handle(ctx, player, data)
                    player.room?.lastOperateMs = now // 对于加入房间类协议，在执行之后需要修改
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
