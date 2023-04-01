package org.tfcc.bingo.message

import org.tfcc.bingo.LinkData
import org.tfcc.bingo.Spell

class SpellListSc(
    val spells: Array<Spell>,
    val time: Long,
    val startTime: Long,
    val gameTime: Int, // 游戏总时间（不含倒计时），单位：分
    val countdown: Int, // 倒计时，单位：秒
    val needWin: Int,
    val whoseTurn: Int,
    val banPick: Int,
    val totalPauseTime: Long?,
    val pauseBeginMs: Long?,
    val status: IntArray?,
    val phase: Int,
    val linkData: LinkData?,
    val difficulty: Int,
    val enableTools: Boolean
)
