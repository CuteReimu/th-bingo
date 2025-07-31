package org.tfcc.bingo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Spell(
    val index: Int,
    val game: String,
    val name: String,
    var rank: String,
    var star: Int,
    var desc: String,
    val id: Int,
    // 以下为AI参数
    val fastest: Float, // 理论最快速度
    @SerialName("miss_time")
    val missTime: Float, // miss平均时间
    @SerialName("power_weight")
    val powerWeight: Float, // 底力系数，熟练度系数=1-底力系数
    val difficulty: Float, // 难度
    @SerialName("change_rate")
    val changeRate: Float, // 变化率，越高说明门槛的性质越强
    @SerialName("max_cap_rate")
    val maxCapRate: Float, // 最高收率
)
