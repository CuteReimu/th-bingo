package org.tfcc.bingo.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RoomConfig(
    /** 房间名 */
    val rid: String,
    /** 1-标准赛，2-BP赛，3-link赛 */
    val type: Int,
    /** 游戏总时间（不含倒计时），单位：分 */
    @SerialName("game_time")
    val gameTime: Int,
    /** 倒计时，单位：秒 */
    val countdown: Int,
    /** 含有哪些作品 */
    var games: Array<String>,
    /** 含有哪些游戏难度，也就是L卡和EX卡 */
    var ranks: Array<String>,
    /** 需要胜利的局数，例如2表示bo3，空表示1 */
    @SerialName("need_win")
    val needWin: Int?,
    /** 难度（影响不同星级的卡的分布），1对应E，2对应N，3对应L，其它对应随机 */
    val difficulty: Int?,
    /** 选卡cd，收卡后要多少秒才能选下一张卡，空表示0 */
    @SerialName("cd_time")
    val cdTime: Int?,
    /** 纯客户端用的一个类型字段 */
    @SerialName("reserved_type")
    val reservedType: Int?,
) {
    @Throws(HandlerException::class)
    fun validate() {
        rid.isNotEmpty() || throw HandlerException("房间名不能为空")
        rid.matches(Regex("""\d{1,16}""")) || throw HandlerException("房间ID不合法")
        type in 1..3 || throw HandlerException("不支持的游戏类型")
        gameTime in 1..1440 || throw HandlerException("游戏时间的数值不正确")
        countdown in 0..86400 || throw HandlerException("倒计时的数值不正确")
        games.size < 100 || throw HandlerException("选择的作品数太多")
        ranks.size <= 6 || throw HandlerException("选择的难度数太多")
        needWin == null || needWin in 1..99 || throw HandlerException("需要胜场的数值不正确")
        cdTime == null || cdTime in 0..1440 || throw HandlerException("选卡cd的数值不正确")
    }
}
