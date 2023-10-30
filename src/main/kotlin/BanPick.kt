package org.tfcc.bingo

import org.tfcc.bingo.message.BanPickInfoSc
import org.tfcc.bingo.message.HandlerException
import org.tfcc.bingo.message.Message
import org.tfcc.bingo.message.writeMessage

/**
 * * 1：A保1（可保ex）
 * * 2：B保1（可保ex）
 * * 3：A保1（不可保ex）
 * * 4：B保1（不可保ex）
 * * 5：Aban1
 * * 6：Bban1
 * * 7：Bban1
 * * 8：Aban1
 * * 9：Aban1
 * * 10：Bban1
 * * 11：同时选择是否开启ex（两人都选完了立即结束，没选完之前一直可以修改）
 * * 9999：结束
 *
 * 流程顺序：
 * * 1->2（如果都保ex）->3->4->5->6->7->8->9->10->9999
 * * 1->2（A保ex）->6->7->10->9999
 * * 1->2（B保ex）->5->8->9->9999
 * * 1->2（都没保ex）->5->6->7->8->9->10->11->9999
 *
 * @param whoFirst 0-左边玩家先，1-右边玩家先
 */
class BanPick(private val whoFirst: Int) {
    var phase = 1
        private set
    private val pick = arrayOf(arrayOf<String>(), arrayOf())
    private val ban = arrayOf(arrayOf<String>(), arrayOf())
    private var openEx = arrayOf(0, 0)

    /**
     * @param playerIndex 左边玩家0，右边玩家1，不是玩家-1
     */
    fun toPb(playerIndex: Int) = BanPickInfoSc(
        whoFirst,
        phase,
        if (phase > 4 || playerIndex != 1) pick[0]
        else if (phase > 2) pick[0].filter { it == "EX" }.toTypedArray()
        else emptyArray(),
        ban[0],
        if (phase > 4 || playerIndex != 0) pick[1]
        else if (phase > 2) pick[1].filter { it == "EX" }.toTypedArray()
        else emptyArray(),
        ban[1],
        openEx[0],
        openEx[1],
    )

    fun notifyAll(room: Room, trigger: Player, protoName: String) {
        for ((idx, token1) in room.players.withIndex()) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(
                Message(
                    reply = if (token1 != trigger.token) null else protoName,
                    trigger = trigger.name,
                    data = toPb(idx)
                )
            )
        }
        for (token1 in listOf(listOf(room.host), room.watchers).flatten()) {
            val conn = Supervisor.getChannel(token1) ?: continue
            conn.writeMessage(
                Message(
                    reply = if (token1 != trigger.token) null else protoName,
                    trigger = trigger.name,
                    data = toPb(-1)
                )
            )
        }
    }

    fun onChoose(index: Int, selection: String): Int {
        if (index != 0 && index != 1) throw HandlerException("服务器内部错误")
        val playerIndex = if (whoFirst == 0) index else 1 - index
        if (phase == 11) {
            openEx[playerIndex] = try {
                selection.toInt().also {
                    if (it != -1 && it != 1) throw HandlerException("参数错误")
                }
            } catch (e: NumberFormatException) {
                throw HandlerException("参数错误：${e.message}")
            }
            if (openEx[0] != 0 && openEx[1] != 0) phase = 9999
            return phase
        }
        if (phase !in turn[playerIndex]) throw HandlerException("没有轮到你")
        if (phase != 1 && phase != 2 && selection == "EX") throw HandlerException("不能选择EX")
        if (selection !in games && selection != "EX") throw HandlerException("参数错误")
        if (phase <= 4) {
            pick[playerIndex] = arrayOf(*pick[playerIndex], selection)
            if (selection == "EX") openEx = arrayOf(1, 1)
        } else {
            if (selection in pick[0] || selection in pick[1]) throw HandlerException("已经保了的作品，不能ban")
            ban[playerIndex] = arrayOf(*ban[playerIndex], selection)
        }
        phase = when (phase) {
            1 -> 2
            2 -> if ("EX" in pick[0] && "EX" in pick[1]) 3 else if ("EX" in pick[0]) 6 else 5
            3 -> 4
            4 -> 5
            5 -> if ("EX" !in pick[0] && "EX" in pick[1]) 8 else 6
            6 -> 7
            7 -> if ("EX" in pick[0] && "EX" !in pick[1]) 10 else 8
            8 -> 9
            9 -> if ("EX" !in pick[0] && "EX" in pick[1]) 9999 else 10
            10 -> if ("EX" !in pick[0] && "EX" !in pick[1]) 11 else 9999
            else -> throw HandlerException("服务器内部错误")
        }
        return phase
    }

    fun getGamesAndRanks(): Pair<Array<String>, Array<String>> {
        val games = games - ban[0].toSet() - ban[1].toSet()
        val ranks = if (openEx[0] == 1 && openEx[1] == 1) arrayOf("L", "EX", "PH") else arrayOf("L")
        return games.toTypedArray() to ranks
    }

    companion object {
        private val turn = arrayOf(listOf(1, 3, 5, 8, 9), listOf(2, 4, 6, 7, 10))
        private val games = listOf("6", "7", "8", "10", "11", "12", "13", "14", "15", "16", "17", "18")
    }
}