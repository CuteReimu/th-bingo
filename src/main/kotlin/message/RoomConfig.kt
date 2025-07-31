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
    /** 特殊模式：双重盘面 */
    @SerialName("dual_board")
    var dualBoard: Int,
    /** 双重盘面设定 */
    @SerialName("portal_count")
    var portalCount: Int,
    /** 盲盒揭示等级 */
    @SerialName("blind_reveal_level")
    var blindRevealLevel: Int,
    /** 双重盘面差异度 */
    @SerialName("diff_level")
    var diffLevel: Int,
    /** 是否启用AI陪练 */
    @SerialName("use_ai")
    val useAI: Boolean,
    /** AI策略难度 */
    @SerialName("ai_strategy_level")
    val aiStrategyLevel: Int, // 1:初级, 2:中级, 3:高级
    /** AI决策风格 */
    @SerialName("ai_style")
    val aiStyle: Int, // 0:默认, 1:进攻型, 2:防守型
    /** AI底力 */
    @SerialName("ai_base_power")
    val aiBasePower: Int,
    /** AI熟练度 */
    @SerialName("ai_experience")
    val aiExperience: Int,
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
        dualBoard in 0..1 || throw HandlerException("双重模式设置不正确")
        portalCount in 1..25 || throw HandlerException("传送门数量应在1~25之间")
        blindRevealLevel in 0..4 || throw HandlerException("盲盒揭示等级应在0~4之间")
        diffLevel in 0..5 || throw HandlerException("盘面差异度等级应在0~5之间")
        aiStrategyLevel in 1..3 || throw HandlerException("AI策略难度设置范围应为1~3")
        aiStyle in 0..2 || throw HandlerException("AI决策风格设置范围应为0~2")
        aiBasePower in 1..10 || throw HandlerException("AI底力设置范围应为1~10")
        aiExperience in 1..10 || throw HandlerException("AI熟练度设置范围应为1~10")
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
            dualBoard = config.dualBoard ?: dualBoard,
            portalCount = config.portalCount ?: portalCount,
            blindRevealLevel = config.blindRevealLevel ?: blindRevealLevel,
            diffLevel = config.diffLevel ?: diffLevel,
            useAI = config.useAI ?: useAI,
            aiStrategyLevel = config.aiStrategyLevel ?: aiStrategyLevel,
            aiStyle = config.aiStyle ?: aiStyle,
            aiBasePower = config.aiBasePower ?: aiBasePower,
            aiExperience = config.aiExperience ?: aiExperience,
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
    /** 特殊模式：双重盘面 */
    @SerialName("dual_board")
    var dualBoard: Int? = null,
    /** 双重盘面设定 */
    @SerialName("portal_count")
    var portalCount: Int? = null,
    /** 盲盒揭示等级 */
    @SerialName("blind_reveal_level")
    var blindRevealLevel: Int? = null,
    /** 双重盘面差异度 */
    @SerialName("diff_level")
    var diffLevel: Int? = null,
    /** 是否启用AI陪练 */
    @SerialName("use_ai")
    val useAI: Boolean? = null, // 0/1
    /** AI策略难度 */
    @SerialName("ai_strategy_level")
    val aiStrategyLevel: Int? = null, // 1:初级, 2:中级, 3:高级
    /** AI决策风格 */
    @SerialName("ai_style")
    val aiStyle: Int? = null, // 0:默认, 1:进攻型, 2:防守型
    /** AI底力 */
    @SerialName("ai_base_power")
    val aiBasePower: Int? = null,
    /** AI熟练度 */
    @SerialName("ai_experience")
    val aiExperience: Int? = null,
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
        dualBoard == null || dualBoard in 0..1 || throw HandlerException("双重模式设置不正确")
        portalCount == null || portalCount in 1..25 || throw HandlerException("传送门数量应在1~25之间")
        blindRevealLevel == null || blindRevealLevel in 0..4 || throw HandlerException("盲盒揭示等级应在0~4之间")
        diffLevel == null || diffLevel in 0..5 || throw HandlerException("盘面差异度等级应在0~5之间")
        aiStrategyLevel == null || aiStrategyLevel in 1..3 || throw HandlerException("AI策略难度设置范围应为1~3")
        aiStyle == null || aiStyle in 0..2 || throw HandlerException("AI决策风格设置范围应为0~2")
        aiBasePower == null || aiBasePower in 1..10 || throw HandlerException("AI底力设置范围应为1~10")
        aiExperience == null || aiExperience in 1..10 || throw HandlerException("AI熟练度设置范围应为1~10")
    }
}
