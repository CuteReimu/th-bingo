package org.tfcc.bingo

import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.Duration
import java.util.*

object MiraiPusher {
    fun push(room: Room) {
        if (!enablePush) return
        val text = "Bingo比赛正在激烈进行，快来围观吧：\n$selfRoomAddr/${room.roomId}"
        runBlocking {
            launch {
                val session = verify()
                bind(session)
                pushQQGroups.forEach { sendGroupMessage(session, it, text) }
                release(session)
            }
        }
    }

    private fun verify(): String {
        val postData = """{"verifyKey":"$miraiVerifyKey"}""".toRequestBody(contentType)
        val request = Request.Builder().url("$miraiHttpUrl/verify").post(postData).build()
        val resp = client.newCall(request).execute()
        val json = gson.fromJson(resp.body!!.string(), JsonElement::class.java)
        val code = json.asJsonObject["code"].asInt
        if (code != 0) throw Exception("verify failed: $code")
        return json.asJsonObject["session"].asString
    }

    private fun bind(sessionKey: String) {
        val postData = """{"sessionKey":"$sessionKey","qq":$robotQQ}""".toRequestBody(contentType)
        val request = Request.Builder().url("$miraiHttpUrl/bind").post(postData).build()
        val resp = client.newCall(request).execute()
        val json = gson.fromJson(resp.body!!.string(), JsonElement::class.java)
        val code = json.asJsonObject["code"].asInt
        if (code != 0) throw Exception("bind failed: $code")
    }

    private fun sendGroupMessage(sessionKey: String, groupId: Long, message: String) {
        val postData = """{
            "sessionKey":"$sessionKey",
            "target":$groupId,
            "messageChain":[{"type":"Plain","text":"$message"}]
        }""".trimMargin().toRequestBody(contentType)
        val request = Request.Builder().url("$miraiHttpUrl/sendGroupMessage").post(postData).build()
        val resp = client.newCall(request).execute()
        val json = gson.fromJson(resp.body!!.string(), JsonElement::class.java)
        val code = json.asJsonObject["code"].asInt
        if (code != 0) throw Exception("sendGroupMessage failed: $code")
    }

    fun release(sessionKey: String) {
        val postData = """{"sessionKey":"$sessionKey","qq":$robotQQ}""".toRequestBody(contentType)
        val request = Request.Builder().url("$miraiHttpUrl/release").post(postData).build()
        val resp = client.newCall(request).execute()
        val json = gson.fromJson(resp.body!!.string(), JsonElement::class.java)
        val code = json.asJsonObject["code"].asInt
        if (code != 0) throw Exception("release failed: $code")
    }

    private val client = OkHttpClient().newBuilder().connectTimeout(Duration.ofMillis(20000)).build()
    private val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val gson = Gson()

    private val enablePush: Boolean
    private val selfRoomAddr: String
    private val miraiHttpUrl: String
    private val miraiVerifyKey: String
    private val robotQQ: Long
    private val pushQQGroups: LongArray

    init {
        val pps = Properties()
        try {
            FileInputStream("application.properties").use { `in` -> pps.load(`in`) }
        } catch (_: FileNotFoundException) {
            // Ignored
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        pps.putIfAbsent("enable_push", "false")
        pps.putIfAbsent("self_room_addr", "http://127.0.0.1:9961/room")
        pps.putIfAbsent("mirai_http_url", "http://127.0.0.1:8080")
        pps.putIfAbsent("mirai_verify_key", "")
        pps.putIfAbsent("robot_qq", "12345678")
        pps.putIfAbsent("push_qq_groups", "")
        enablePush = pps.getProperty("enable_push").toBoolean()
        selfRoomAddr = pps.getProperty("self_room_addr")
        miraiHttpUrl = pps.getProperty("mirai_http_url")
        miraiVerifyKey = pps.getProperty("mirai_verify_key")
        robotQQ = pps.getProperty("robot_qq").toLong()
        pushQQGroups = pps.getProperty("push_qq_groups").run {
            if (isEmpty()) return@run longArrayOf()
            split(",").map(String::toLong).toLongArray()
        }
        try {
            FileOutputStream("application.properties").use { out -> pps.store(out, "application.properties") }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}