package org.tfcc.bingo

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.tfcc.bingo.SpellStatus.*
import org.tfcc.bingo.message.HandlerException
import org.tfcc.bingo.message.NormalData
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.asKotlinRandom

object RoomTypeNormal : RoomType {
    override val name = "标准赛"

    override val canPause = true

    val spellStatusBackup = Array(25) { NONE }

    override fun onStart(room: Room) {
        room.normalData = NormalData()
        room.spells2 = emptyArray()
        if (room.roomConfig.dualBoard > 0) {
            handleDualExistRandomCardSettings(room)
        }
        if (room.roomConfig.blindSetting > 1) {
            handleBlindSettings(room)
        }
    }

    private fun handleBlindSettings(room: Room) {
        room.spellStatus = Array(room.spells!!.size) { BOTH_HIDDEN }
        val outerRingIndex = arrayOf(0, 1, 2, 3, 4, 5, 9, 10, 14, 15, 19, 20, 21, 22, 23, 24)
        val innerRingIndex = arrayOf(6, 7, 8, 11, 13, 16, 17, 18)
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        outerRingIndex.shuffle(rand)
        innerRingIndex.shuffle(rand)

        if (room.roomConfig.blindSetting == 2) {
            val reveal = Array(5) { IntArray(4) }
            // 外环单独，外环共有，内环单独，内环共有
            reveal[0] = intArrayOf(0, 0, 0, 0)
            reveal[1] = intArrayOf(2, 1, 1, 0)
            reveal[2] = intArrayOf(3, 1, 1, 1)
            reveal[3] = intArrayOf(4, 2, 2, 1)
            reveal[4] = intArrayOf(5, 4, 2, 2)
            val level = room.roomConfig.blindRevealLevel
            var index = 0
            // 外环
            for (i in 0 until reveal[level][0]) {
                room.spellStatus!![outerRingIndex[i]] = LEFT_SEE_ONLY
            }
            index += reveal[level][0]
            for (i in index until index + reveal[level][0]) {
                room.spellStatus!![outerRingIndex[i]] = RIGHT_SEE_ONLY
            }
            index += reveal[level][0]
            for (i in index until index + reveal[level][1]) {
                room.spellStatus!![outerRingIndex[i]] = NONE
            }
            // 内环
            index = 0
            for (i in 0 until reveal[level][2]) {
                room.spellStatus!![innerRingIndex[i]] = LEFT_SEE_ONLY
            }
            index += reveal[level][2]
            for (i in index until index + reveal[level][2]) {
                room.spellStatus!![innerRingIndex[i]] = RIGHT_SEE_ONLY
            }
            index += reveal[level][2]
            for (i in index until index + reveal[level][3]) {
                room.spellStatus!![innerRingIndex[i]] = NONE
            }
        } else if (room.roomConfig.blindSetting == 3) {
            val reveal = Array(5) { IntArray(6) }
            // 外环作品，内环面数，外环作品，内环面数，外环全部，内环全部
            reveal[0] = intArrayOf(8, 4, 0, 0, 0, 0)
            reveal[1] = intArrayOf(16, 8, 0, 0, 0, 0)
            reveal[2] = intArrayOf(8, 4, 8, 4, 0, 0)
            reveal[3] = intArrayOf(0, 0, 16, 8, 0, 0)
            reveal[4] = intArrayOf(0, 0, 12, 6, 4, 2)
            val level = room.roomConfig.blindRevealLevel
            // 外环
            var index = 0
            for (i in 0 until reveal[level][0]) {
                room.spellStatus!![outerRingIndex[i]] = ONLY_REVEAL_GAME
            }
            index += reveal[level][0]
            for (i in index until index + reveal[level][2]) {
                room.spellStatus!![outerRingIndex[i]] = ONLY_REVEAL_GAME_STAGE
            }
            index += reveal[level][2]
            for (i in index until index + reveal[level][4]) {
                room.spellStatus!![outerRingIndex[i]] = NONE
            }
            // 内环
            index = 0
            for (i in 0 until reveal[level][1]) {
                room.spellStatus!![innerRingIndex[i]] = ONLY_REVEAL_GAME
            }
            index += reveal[level][1]
            for (i in index until index + reveal[level][3]) {
                room.spellStatus!![innerRingIndex[i]] = ONLY_REVEAL_GAME_STAGE
            }
            index += reveal[level][3]
            for (i in index until index + reveal[level][5]) {
                room.spellStatus!![innerRingIndex[i]] = NONE
            }
        }
        room.spellStatus!!.forEachIndexed { index, status -> spellStatusBackup[index] = status }
    }

    private fun handleDualExistRandomCardSettings(room: Room) {
        // rewrite the roll spell logic. We generate spellStarArray by individual calls
        val starArray = rollSpellsStarArray(room.roomConfig.difficulty)
        rollSpellCard(room, starArray)
        // only room.spells can be assigned in rollSpellCard, so spells2 can only copy from spells
        room.spells2 = room.spells!!.copyOf()
        // calculate maximum approx diff
        var targetDiff: Int
        if (room.roomConfig.difficulty!! == 4) {
            targetDiff = 52
        } else {
            val lv3count = room.spells!!.sumOf { abs(if (it.star == 3) 1 else 0) }
            targetDiff = if (lv3count < 7) 26 + lv3count * 2 else min(40, 58 - lv3count * 2)
        }
        // generate another starArray
        val spell2RankArray = SimilarBoardGenerator.findMatrixB(starArray, (targetDiff * room.roomConfig.diffLevel + 2) / 5)
        // reassign room.spells
        rollSpellCard(room, spell2RankArray)

        // 转换格设定
        val outerRingIndex = arrayOf(0, 1, 2, 3, 4, 5, 9, 10, 14, 15, 19, 20, 21, 22, 23, 24)
        val innerRingIndex = arrayOf(6, 7, 8, 11, 12, 13, 16, 17, 18)
        val innerCount = room.roomConfig.portalCount * 9 / 25
        val outerCount = room.roomConfig.portalCount - innerCount
        val rand = ThreadLocalRandom.current().asKotlinRandom()
        outerRingIndex.shuffle(rand)
        innerRingIndex.shuffle(rand)
        for (i in 0 until outerCount) {
            room.normalData!!.isPortalA[outerRingIndex[i]] = 1
        }
        for (i in 0 until innerCount) {
            room.normalData!!.isPortalA[innerRingIndex[i]] = 1
        }

        outerRingIndex.shuffle(rand)
        innerRingIndex.shuffle(rand)
        for (i in 0 until outerCount) {
            room.normalData!!.isPortalB[outerRingIndex[i]] = 1
        }
        for (i in 0 until innerCount) {
            room.normalData!!.isPortalB[innerRingIndex[i]] = 1
        }
    }

    override fun handleNextRound(room: Room) {
        throw HandlerException("不支持下一回合的游戏类型")
    }

    @Throws(HandlerException::class)
    override fun randSpells(spellCardVersion: Int, games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell> {
        if (difficulty == 4)
            return SpellFactory.randSpellsOD(
                spellCardVersion, games, ranks
            )
        return SpellFactory.randSpells(
            spellCardVersion, games, ranks, when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
    }

    override fun rollSpellsStarArray(difficulty: Int?): IntArray {
        if (difficulty == 4)
            return SpellFactory.randSpellsODStarArray()
        return SpellFactory.randSpellsStarArray(
            when (difficulty) {
                1 -> Difficulty.E
                2 -> Difficulty.N
                3 -> Difficulty.L
                else -> Difficulty.random()
            }
        )
    }

    override fun randSpellsWithStar(
        spellCardVersion: Int,
        games: Array<String>,
        ranks: Array<String>,
        difficulty: Int?,
        stars: IntArray?
    ): Array<Spell> {
        if (stars == null) {
            return randSpells(spellCardVersion, games, ranks, difficulty)
        }
        if (difficulty == 4)
            return SpellFactory.randSpellsODWithStar(
                spellCardVersion, games, ranks, stars
            )
        return SpellFactory.randSpellsWithStar(
            spellCardVersion, games, ranks, stars
        )
    }

    override fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int) {
        var st = room.spellStatus!![spellIndex]
        if (playerIndex == 1) st = st.opposite()
        room.pauseBeginMs == 0L || throw HandlerException("暂停中，不能操作")
        var now = System.currentTimeMillis()
        val gameTime = room.roomConfig.gameTime.toLong() * 60000L
        val countdown = room.roomConfig.countdown.toLong() * 1000
        if (room.startMs <= now - gameTime - countdown - room.totalPauseMs &&
            !room.isHost(room.players[playerIndex]!!) && !room.scoreDraw()
        )
            throw HandlerException("游戏时间到")
        if (now < room.startMs + countdown) {
            if (st == RIGHT_SELECT) throw HandlerException("倒计时阶段不能抢卡")
        }
        // 选卡CD
        val cdTime = room.roomConfig.cdTime ?: 0
        if (cdTime > 0) {
            val lastGetTime = room.lastGetTime[playerIndex]
            val nextCanSelectTime = lastGetTime + (cdTime - 1) * 1000L // 服务器扣一秒，以防网络延迟
            val remainSelectTime = nextCanSelectTime - now
            if (remainSelectTime > 0)
                throw HandlerException("还有${remainSelectTime / 1000 + 1}秒才能选卡")
        }

        room.spellStatus!![spellIndex] = when (st) {
            LEFT_GET -> throw HandlerException("你已打完")
            RIGHT_GET -> throw HandlerException("对方已打完")
            NONE -> LEFT_SELECT
            LEFT_SELECT -> throw HandlerException("重复选卡")
            BOTH_SELECT, RIGHT_SELECT -> BOTH_SELECT
            BOTH_HIDDEN, LEFT_SEE_ONLY, RIGHT_SEE_ONLY -> LEFT_SELECT
            ONLY_REVEAL_GAME, ONLY_REVEAL_GAME_STAGE, ONLY_REVEAL_STAR -> LEFT_SELECT
            else -> throw HandlerException("状态错误：$st")
        }.run { if (playerIndex == 1) opposite() else this }

        return
        /*
        // 无导播模式不记录
        room.host != null || return
        // 等操作结束后再记录
        if (now < room.startMs + countdown) {
            // 倒计时没结束，需要按照倒计时已经结束的时间点计算开始收卡的时间
            now = room.startMs + countdown
        }
        val playerName = room.players[playerIndex]!!.name
        var status = LEFT_SELECT
        if (playerIndex == 1) status = status.opposite()
        SpellLog.logSpellOperate(status, room.spells!![spellIndex], playerName, now, SpellLog.GameType.NORMAL)
         */
    }

    override fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean) {
        success || throw HandlerException("标准赛不支持收卡失败的操作")
        playerIndex >= 0 || throw HandlerException("只有玩家才能主动收卡")
        var st = room.spellStatus!![spellIndex]
        if (playerIndex == 1) st = st.opposite()
        room.pauseBeginMs == 0L || throw HandlerException("暂停中，不能操作")
        var now = System.currentTimeMillis()
        val gameTime = room.roomConfig.gameTime.toLong() * 60000L
        val countdown = room.roomConfig.countdown.toLong() * 1000
        if (room.startMs <= now - gameTime - countdown - room.totalPauseMs &&
            !room.isHost(room.players[playerIndex]!!) && !room.scoreDraw()
        )
            throw HandlerException("游戏时间到")
        if (now < room.startMs + countdown) {
            throw HandlerException("倒计时还没结束")
        }

        room.spellStatus!![spellIndex] = when (st) {
            LEFT_GET -> throw HandlerException("你已打完")
            RIGHT_GET -> throw HandlerException("对方已打完")
            NONE, RIGHT_SELECT -> throw HandlerException("你还未选卡")
            BOTH_SELECT, LEFT_SELECT -> LEFT_GET
            BOTH_HIDDEN, LEFT_SEE_ONLY, RIGHT_SEE_ONLY -> throw HandlerException("你还未选卡")
            ONLY_REVEAL_GAME, ONLY_REVEAL_GAME_STAGE, ONLY_REVEAL_STAR -> throw HandlerException("你还未选卡")
            else -> throw HandlerException("状态错误：$st")
        }.run { if (playerIndex == 1) opposite() else this }

        room.lastGetTime[playerIndex] = now // 更新上次收卡时间

        if (room.roomConfig.dualBoard <= 0) return
        // 记录是谁在哪个盘面上收取的
        val boardIndex = if (playerIndex == 0) room.normalData!!.whichBoardA else room.normalData!!.whichBoardB
        room.normalData!!.getOnWhichBoard[spellIndex] = when (playerIndex * 2 + boardIndex) {
            0 -> 0x1
            1 -> 0x2
            2 -> 0x10
            3 -> 0x20
            else -> throw HandlerException("错误的收取盘面记录")
        }
        // 如果是传送门格，更改该玩家的盘面
        // 如果是A玩家在A面收取一张A传送门卡，或者...
        if (playerIndex == 0) {
            if (room.normalData!!.whichBoardA == 0 && room.normalData!!.isPortalA[spellIndex] > 0)
                room.normalData!!.whichBoardA = 1
            else if (room.normalData!!.whichBoardA == 1 && room.normalData!!.isPortalB[spellIndex] > 0)
                room.normalData!!.whichBoardA = 0
        } else if (playerIndex == 1) {
            if (room.normalData!!.whichBoardB == 0 && room.normalData!!.isPortalA[spellIndex] > 0)
                room.normalData!!.whichBoardB = 1
            else if (room.normalData!!.whichBoardB == 1 && room.normalData!!.isPortalB[spellIndex] > 0)
                room.normalData!!.whichBoardB = 0
        }
        // Finish Spell 会调用pushSpell推送盘面更改结果，视觉改变交由前端处理

        return
        /*
        // 无导播模式不记录
        room.host != null || return
        // 等操作结束后再记录
        if (now < room.startMs + countdown) {
            // 倒计时没结束，需要按照倒计时已经结束的时间点计算开始收卡的时间
            now = room.startMs + countdown
        }
        val playerName = room.players[playerIndex]!!.name
        var status = LEFT_GET
        if (playerIndex == 1) status = status.opposite()
        SpellLog.logSpellOperate(status, room.spells!![spellIndex], playerName, now, SpellLog.GameType.NORMAL)
         */
    }

    /**
     * 收了一定数量的卡之后，隐藏对方的选卡
     */
    private fun formatSpellStatus(room: Room, status: SpellStatus, playerIndex: Int): Int {
        var st = status
        if (st.isSelectStatus()) {
            if ((room.roomConfig.reservedType ?: 0) == 0) {
                // 个人赛对方收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0 && room.spellStatus!!.count { it == RIGHT_GET } >= 5) {
                    if (status == RIGHT_SELECT) st = NONE
                    else if (status == BOTH_SELECT) st = LEFT_SELECT
                } else if (playerIndex == 1 && room.spellStatus!!.count { it == LEFT_GET } >= 5) {
                    if (status == LEFT_SELECT) st = NONE
                    else if (status == BOTH_SELECT) st = RIGHT_SELECT
                }
            } else if (room.spellStatus!!.count { it == LEFT_GET || it == RIGHT_GET } >= 5) {
                // 团体赛双方合计收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0) {
                    if (status == RIGHT_SELECT) st = NONE
                    else if (status == BOTH_SELECT) st = LEFT_SELECT
                } else if (playerIndex == 1) {
                    if (status == LEFT_SELECT) st = NONE
                    else if (status == BOTH_SELECT) st = RIGHT_SELECT
                }
            }
        }
        return st.value
    }

    /**
     * 仅对选手生效
     * 收了一定数量的卡之后，隐藏对方的选卡
     * 前五张对方的选卡不是HIDDEN状态，只要选择就是双方可见状态
     * 而五张之后对方选卡不可见，若处于自己视野之外则为HIDDEN，否则为NONE。
     */
    private fun formatSpellStatus2(room: Room, status: SpellStatus, playerIndex: Int, spellIndex: Int): Int {
        var st = status
        // 如果是对称的可见情况，隐藏选卡
        if (st.isSelectStatus()) {
            if ((room.roomConfig.reservedType ?: 0) == 0) {
                // 个人赛对方收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0 && room.spellStatus!!.count { it == RIGHT_GET } >= 5) {
                    if (status == RIGHT_SELECT)
                        st = decideStatus(room, spellIndex, false)
                    else if (status == BOTH_SELECT) st = LEFT_SELECT
                } else if (playerIndex == 1 && room.spellStatus!!.count { it == LEFT_GET } >= 5) {
                    if (status == LEFT_SELECT)
                        st = decideStatus(room, spellIndex, true)
                    else if (status == BOTH_SELECT) st = RIGHT_SELECT
                }
            } else if (room.spellStatus!!.count { it == LEFT_GET || it == RIGHT_GET } >= 5) {
                // 团体赛双方合计收了五张卡之后，不再可以看到对方的选卡
                if (playerIndex == 0) {
                    if (status == RIGHT_SELECT)
                        st = decideStatus(room, spellIndex, false)
                    else if (status == BOTH_SELECT) st = LEFT_SELECT
                } else if (playerIndex == 1) {
                    if (status == LEFT_SELECT)
                        st = decideStatus(room, spellIndex, true)
                    else if (status == BOTH_SELECT) st = RIGHT_SELECT
                }
            }
        }
        // 如果是不对称的可见情况，将当前能看到的改为NONE，否则为HIDDEN
        if (st == LEFT_SEE_ONLY) {
            st = if (playerIndex == 0) NONE else BOTH_HIDDEN
        } else if (st == RIGHT_SEE_ONLY) {
            st = if (playerIndex == 1) NONE else BOTH_HIDDEN
        }
        return st.value
    }

    private fun decideStatus(room: Room, spellIndex: Int, isLeftSelect: Boolean): SpellStatus {
        if (room.roomConfig.blindSetting == 2) {
            if (spellStatusBackup[spellIndex] == NONE ||
                (spellStatusBackup[spellIndex] == LEFT_SEE_ONLY && !isLeftSelect) ||
                (spellStatusBackup[spellIndex] == RIGHT_SEE_ONLY && isLeftSelect)
            )
                return NONE
            else return BOTH_HIDDEN
        } else if (room.roomConfig.blindSetting == 3) {
            return spellStatusBackup[spellIndex]
        }
        return NONE
    }

    override fun pushSpells(room: Room, spellIndex: Int, causer: String) {
        val status = room.spellStatus!![spellIndex]
        val allStatus = JsonObject(
            mapOf(
                "index" to JsonPrimitive(spellIndex),
                "status" to JsonPrimitive(status.value),
                "causer" to JsonPrimitive(causer),
                // 收取符卡后可能改变：玩家所处的版面、收取记录
                "which_board_a" to JsonPrimitive(room.normalData!!.whichBoardA),
                "which_board_b" to JsonPrimitive(room.normalData!!.whichBoardB),
                "get_on_which_board" to JsonPrimitive(room.normalData!!.getOnWhichBoard[spellIndex]),
            )
        )
        room.host?.push("push_update_spell_status", allStatus)
        for (i in room.players.indices) {
            val oldStatus = room.spellStatusInPlayerClient!![i][spellIndex]
            val newStatus = if (room.roomConfig.blindSetting == 1) formatSpellStatus(room, status, i)
            else formatSpellStatus2(room, status, i, spellIndex)
            if (oldStatus != newStatus) {
                room.players[i]?.push(
                    "push_update_spell_status", JsonObject(
                        mapOf(
                            "index" to JsonPrimitive(spellIndex),
                            "status" to JsonPrimitive(newStatus),
                            "causer" to JsonPrimitive(causer),
                            "which_board_a" to JsonPrimitive(room.normalData!!.whichBoardA),
                            "which_board_b" to JsonPrimitive(room.normalData!!.whichBoardB),
                            "get_on_which_board" to JsonPrimitive(room.normalData!!.getOnWhichBoard[spellIndex]),
                        )
                    )
                )
                room.spellStatusInPlayerClient!![i][spellIndex] = newStatus
            }
        }
        room.watchers.forEach { it.push("push_update_spell_status", allStatus) }
    }

    override fun getAllSpellStatus(room: Room, playerIndex: Int): List<Int> {
        if (playerIndex == -1)
            return super.getAllSpellStatus(room, playerIndex)
        return if (room.roomConfig.blindSetting == 1)
            room.spellStatus!!.map { formatSpellStatus(room, it, playerIndex) }
        else room.spellStatus!!.mapIndexed { index, status -> formatSpellStatus2(room, status, playerIndex, index) }
    }

    override fun updateSpellStatusPostProcesser(
        room: Room,
        player: Player,
        spellIndex: Int,
        prevStatus: SpellStatus,
        status: SpellStatus
    ) {
        // 盲盒模式1中，单方面选择并取消选卡会使符卡回归初始状态
        if (room.roomConfig.blindSetting == 2) {
            if (status == NONE && prevStatus.isOneSelectStatus()) {
                room.spellStatus!![spellIndex] = spellStatusBackup[spellIndex]
            }
        }
        // 盲盒模式2中，不允许出现NONE状态。翻回去的牌返回初始状态（如果不是BOTH_SELECT）
        if (room.roomConfig.blindSetting == 3) {
            if (status == NONE && prevStatus.isOneSelectStatus()) {
                room.spellStatus!![spellIndex] = spellStatusBackup[spellIndex]
            }
        }
    }

    /** 是否平局 */
    private fun Room.scoreDraw(): Boolean {
        var left = 0
        spellStatus!!.forEach {
            if (it == LEFT_GET) left++
            else if (it == RIGHT_GET) left--
        }
        return left == 0
    }
}
