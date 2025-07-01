package org.tfcc.bingo

import org.apache.logging.log4j.kotlin.logger
import org.tfcc.bingo.message.HandlerException

sealed interface RoomType {
    val name: String

    @Throws(HandlerException::class)
    fun rollSpellCard(room: Room, stars: IntArray? = null) {
        val start = System.currentTimeMillis()
        var retryCount = 0
        while (true) {
            try {
                if (stars == null) {
                    room.spells = room.type.randSpells(
                        room.roomConfig.spellCardVersion, room.roomConfig.games,
                        room.roomConfig.ranks, room.roomConfig.difficulty
                    )
                } else {
                    room.spells = room.type.randSpellsWithStar(
                        room.roomConfig.spellCardVersion, room.roomConfig.games,
                        room.roomConfig.ranks, room.roomConfig.difficulty, stars
                    )
                }
                break
            } catch (e: HandlerException) {
                if (++retryCount >= 10 || e.message != "符卡数量不足") {
                    logger.error("随符卡失败", e)
                    throw e
                }
            }
        }
        val now = System.currentTimeMillis()
        logger.debug("随符卡耗时：${now - start}")
        /*
        val debugSpells = room.debugSpells
        if (debugSpells != null) { // 测试用强制选符卡
            val roomType = SpellConfig.NORMAL_GAME
            room.spells!!.forEachIndexed { i, _ ->
                if (debugSpells[i] != 0) {
                    SpellConfig.getSpellById(
                        roomType, room.roomConfig.spellCardVersion, debugSpells[i])?.also { room.spells!![i] = it }
                }
            }
        }
         */
        // SpellLog.logRandSpells(room.spells!!, room.type)
    }

    fun initStatus(room: Room) {
        room.started = true
        room.startMs = System.currentTimeMillis()
        room.spellStatus = Array(room.spells!!.size) { SpellStatus.NONE }
        room.spellStatusInPlayerClient = Array(room.players.size) { room.spellStatus!!.map { it.value }.toIntArray() }
        room.locked = true
        room.banPick = null
    }

    fun resetData(room: Room) {
        room.spells = null
        room.spells2 = null
        room.normalData = null
        room.bpData = null
        room.linkData = null
    }

    fun onStart(room: Room)

    @Throws(HandlerException::class)
    fun handleNextRound(room: Room)

    val canPause: Boolean

    @Throws(HandlerException::class)
    fun randSpells(spellCardVersion: Int, games: Array<String>, ranks: Array<String>, difficulty: Int?): Array<Spell>

    @Throws(HandlerException::class)
    fun randSpellsWithStar(
        spellCardVersion: Int,
        games: Array<String>,
        ranks: Array<String>,
        difficulty: Int?,
        stars: IntArray?
    ): Array<Spell> {
        return randSpells(spellCardVersion, games, ranks, difficulty)
    }

    fun rollSpellsStarArray(difficulty: Int?): IntArray {
        return intArrayOf()
    }

    /**
     * @param banSelect true-ban操作，false-选卡操作
     */
    @Throws(HandlerException::class)
    fun handleSelectSpell(room: Room, playerIndex: Int, spellIndex: Int)

    @Throws(HandlerException::class)
    fun handleFinishSpell(room: Room, isHost: Boolean, playerIndex: Int, spellIndex: Int, success: Boolean)

    /** 向房间内所有玩家推送符卡状态 */
    fun pushSpells(room: Room, spellIndex: Int, causer: String)

    /**
     * 获取所有符卡状态
     *
     * @param playerIndex 0:左侧玩家，1:右侧玩家，-1:不是玩家
     */
    fun getAllSpellStatus(room: Room, playerIndex: Int): List<Int> {
        return room.spellStatus!!.map { it.value }
    }

    /** 有符卡的状态被直接设定时，应如何进行后处理 */
    fun updateSpellStatusPostProcesser(
        room: Room,
        player: Player,
        spellIndex: Int,
        prevStatus: SpellStatus,
        status: SpellStatus
    ) {
    }
}
