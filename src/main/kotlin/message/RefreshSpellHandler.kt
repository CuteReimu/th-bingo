package message

import io.netty.channel.ChannelHandlerContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tfcc.bingo.*
import org.tfcc.bingo.message.HandlerException

object RefreshSpellHandler : RequestHandler {
    @Throws(HandlerException::class)
    override fun handle(ctx: ChannelHandlerContext, player: Player, data: JsonElement?): JsonElement? {
        val m = data!!.jsonObject
        val room = player.room ?: throw HandlerException("不在房间里")
        room.started || throw HandlerException("游戏还没开始")
        if (room.host != null) { // 自己是房主则有权限
            room.host === player || throw HandlerException("没有权限")
        } else { // 无房主模式，只要是选手就有权限
            player in room.players || throw HandlerException("没有权限")
        }
        if (room.host === player && room.linkData != null) {
            if (!room.linkData!!.selectCompleteA() || room.linkData!!.selectCompleteB()) {
                throw HandlerException("link赛符卡还未选完，暂不能操作")
            }
        }

        val boardIdx = m["board_idx"]!!.jsonPrimitive.int
        val spellIdx = m["spell_idx"]!!.jsonPrimitive.int
        boardIdx in 0..1 || throw HandlerException("版面选择超出范围")
        spellIdx in 0..24 || throw HandlerException("符卡选择超出范围")

        val spell = if (boardIdx == 0) room.spells?.get(spellIdx) else room.spells2?.get(spellIdx)
        val newSpell = if (boardIdx == 0) spell?.let { room.refreshManager1?.refreshSpell(it) }
        else spell?.let { room.refreshManager2?.refreshSpell(it) }
        if (newSpell == null) {
            throw HandlerException("没有可以替换的符卡")
        } else {
            if (boardIdx == 0) room.spells?.set(spellIdx, newSpell)
            else room.spells2?.set(spellIdx, newSpell)
            room.push("push_update_one_spell",
                OneSpellsResponse(boardIdx, spellIdx, newSpell, player.name).encode())
        }
        return null
    }
}

@Serializable
class OneSpellsResponse(
    @SerialName("board_idx")
    val boardIdx: Int,
    @SerialName("spell_idx")
    val spellIdx: Int,
    @SerialName("spell")
    val spell: Spell,
    @SerialName("player_name")
    val name: String,
)
