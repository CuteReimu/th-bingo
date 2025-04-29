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
    /** 是否为团体赛 */
    @SerialName("reserved_type")
    val reservedType: Int? = null,
    /** 盲盒设置（对标准/BP生效） */
    @SerialName("blind_setting")
    var blindSetting: Int,
    /** 题库版本 */
    @SerialName("spell_version")
    var spellCardVersion: Int,
) {
    @Throws(HandlerException::class)
    fun validate() {
        rid.isNotEmpty() || throw HandlerException("房间名不能为空")
        type in 1..3 || throw HandlerException("不支持的游戏类型")
        gameTime in 1..1440 || throw HandlerException("游戏时间的数值不正确")
        countdown in 0..86400 || throw HandlerException("倒计时的数值不正确")
        games.size < 100 || throw HandlerException("选择的作品数太多")
        ranks.size <= 6 || throw HandlerException("选择的难度数太多")
        needWin == null || needWin in 1..99 || throw HandlerException("需要胜场的数值不正确")
        cdTime == null || cdTime in 0..1440 || throw HandlerException("选卡cd的数值不正确")
        blindSetting in 1..3 || throw HandlerException("盲盒模式设置不正确")
        spellCardVersion in 1..10 || throw HandlerException("题库版本选择不正确")
    }

    operator fun plus(config: RoomConfigNullable): RoomConfig {
        return RoomConfig(
            rid = config.rid,
            type = config.type ?: type,
            gameTime = config.gameTime ?: gameTime,
            countdown = config.countdown ?: countdown,
            games = config.games ?: games,
            ranks = config.ranks ?: ranks,
            needWin = config.needWin ?: needWin,
            difficulty = config.difficulty ?: difficulty,
            cdTime = config.cdTime ?: cdTime,
            reservedType = config.reservedType ?: reservedType,
            blindSetting = config.blindSetting ?: blindSetting,
            spellCardVersion = config.spellCardVersion ?: spellCardVersion,
        )
    }
}

@Serializable
class RoomConfigNullable(
    /** 房间名 */
    val rid: String,
    /** 1-标准赛，2-BP赛，3-link赛 */
    val type: Int? = null,
    /** 游戏总时间（不含倒计时），单位：分 */
    @SerialName("game_time")
    val gameTime: Int? = null,
    /** 倒计时，单位：秒 */
    val countdown: Int? = null,
    /** 含有哪些作品 */
    val games: Array<String>? = null,
    /** 含有哪些游戏难度，也就是L卡和EX卡 */
    val ranks: Array<String>? = null,
    /** 需要胜利的局数，例如2表示bo3，空表示1 */
    @SerialName("need_win")
    val needWin: Int? = null,
    /** 难度（影响不同星级的卡的分布），1对应E，2对应N，3对应L，其它对应随机 */
    val difficulty: Int? = null,
    /** 选卡cd，收卡后要多少秒才能选下一张卡，空表示0 */
    @SerialName("cd_time")
    val cdTime: Int? = null,
    /** 是否为团体赛 */
    @SerialName("reserved_type")
    val reservedType: Int? = null,
    /** 盲盒设置（暂时只对标准生效） */
    @SerialName("blind_setting")
    var blindSetting: Int? = null,
    /** 题库版本 */
    @SerialName("spell_version")
    var spellCardVersion: Int? = null,
) {
    @Throws(HandlerException::class)
    fun validate() {
        rid.isNotEmpty() || throw HandlerException("房间名不能为空")
        type == null || type in 1..3 || throw HandlerException("不支持的游戏类型")
        gameTime == null || gameTime in 1..1440 || throw HandlerException("游戏时间的数值不正确")
        countdown == null || countdown in 0..86400 || throw HandlerException("倒计时的数值不正确")
        games == null || games.size < 100 || throw HandlerException("选择的作品数太多")
        ranks == null || ranks.size <= 6 || throw HandlerException("选择的难度数太多")
        needWin == null || needWin in 1..99 || throw HandlerException("需要胜场的数值不正确")
        cdTime == null || cdTime in 0..1440 || throw HandlerException("选卡cd的数值不正确")
        blindSetting == null || blindSetting in 1..3 || throw HandlerException("盲盒模式设置不正确")
        spellCardVersion == null || spellCardVersion in 1..10 || throw HandlerException("题库版本选择不正确")
    }
}
