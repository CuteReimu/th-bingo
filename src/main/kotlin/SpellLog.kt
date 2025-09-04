package org.tfcc.bingo

import org.apache.logging.log4j.kotlin.logger
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 记录每张卡的出现次数，选择次数，收取次数，平均收取时长
 */
object SpellLog {
    private val logList = ArrayList<HashMap<String, LogModel>>()

    /** 收卡时间记录 */
    private val timeLogs = HashMap<String, SpellTimeStamp>()

    init {
        readFile()
    }

    fun logRandSpells(cards: Array<Spell>, roomType: RoomType) {
        try {
            for (card in cards) {
                logSpell(LogType.APPEAR, card, gameType = GameType.getGameType(roomType))
            }
        } catch (e: Exception) {
            logger.error("log rand spells failed: ", e)
        }
    }

    fun logSpellOperate(spellStatus: Int, spell: Spell, name: String, time: Long = System.currentTimeMillis(), gameType: Int) {
        try {
            if (spellStatus.isSelectStatus()) {
                logSpell(LogType.SELECT, spell, name, time, gameType = gameType)
            }
            if (spellStatus.isGetStatus()) {
                logSpell(LogType.GET, spell, name, gameType = gameType)
            }
        } catch (e: Exception) {
            logger.error("log spell operate failed: ", e)
        }
    }

    /** 一场比赛结束时存储，此时没收掉的卡就无视 */
    fun saveFile() {
        try {
            timeLogs.clear()
            val file = File("log.xlsx")
            if (!file.exists()) file.createNewFile()
            val os = ByteArrayOutputStream()
            XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ_WRITE)).use { wb ->
                for (i in 0 until logList.size) {
                    val sheet = wb.getSheetAt(i)
                    for (model in logList[i]) {
                        if (!model.value.changed) continue
                        val row = sheet.getRow(model.value.id)
                        with(row) {
                            getCell(1)?.setCellValue(model.value.appear.toDouble())
                                ?: createCell(1).setCellValue(model.value.appear.toDouble())
                            getCell(2)?.setCellValue(model.value.select.toDouble())
                                ?: createCell(2).setCellValue(model.value.select.toDouble())
                            getCell(3)?.setCellValue(model.value.get.toDouble())
                                ?: createCell(3).setCellValue(model.value.get.toDouble())
                            getCell(4)?.setCellValue(model.value.time.toDouble())
                                ?: createCell(4).setCellValue(model.value.time.toDouble())
                        }
                    }
                }
                wb.write(os)
            }
            file.writeBytes(os.toByteArray())
        } catch (e: Exception) {
            logger.error("save file failed: ", e)
        }
    }

    private fun logSpell(
        type: LogType,
        card: Spell,
        name: String = "",
        time: Long = System.currentTimeMillis(),
        gameType: Int = GameType.NORMAL
    ) {
        when (type) {
            LogType.APPEAR -> {
                logList[gameType][card.name]?.let {
                    logList[gameType].put(card.name, it.addAppear())
                }
            }

            LogType.SELECT -> {
                logList[gameType][card.name] = logList[gameType][card.name]!!.addSelect()
                if (gameType == GameType.NORMAL) timeLogs[name] = SpellTimeStamp(time)
            }

            LogType.GET -> {
                if (gameType == GameType.NORMAL) {
                    timeLogs[name]?.let {
                        logList[gameType][card.name] = logList[gameType][card.name]!!.getCard(time - it.start)
                    }
                    timeLogs.remove(name)
                } else {
                    logList[gameType][card.name] = logList[gameType][card.name]!!.getCard()
                }
            }
        }
        logList[gameType][card.name]!!.changed = true
    }

    private fun readFile() {
        val file = File("log.xlsx")
        if (!file.exists()) {
            createLogFile()
        }
        XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ)).use { wb ->
            (0..2).map {
                logList.add(HashMap())
                it < wb.numberOfSheets || return@map
                val sheet = wb.getSheetAt(it)
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    logList[it][row.getCell(0).stringCellValue.trim()] = LogModel(
                        id = i,
                        appear = row.getCell(1)?.numericCellValue?.toInt() ?: 0,
                        select = row.getCell(2)?.numericCellValue?.toInt() ?: 0,
                        get = row.getCell(3)?.numericCellValue?.toInt() ?: 0,
                        time = row.getCell(4)?.numericCellValue?.toLong() ?: 0,
                    )
                }
            }
        }
    }

    /** 根据已有的文件生产log文件 */
    fun createLogFile() {
        try {
            val logFile = File("log.xlsx")
            XSSFWorkbook().use { logWB ->
                val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
                for (file in files) {
                    if (file.extension != "xlsx" || file.name.startsWith("log")) continue
                    logWB.createSheet("normal")
                    logWB.createSheet("bp")
                    logWB.createSheet("link")
                    XSSFWorkbook(OPCPackage.open(file, PackageAccess.READ)).use { wb ->
                        wb.missingCellPolicy = Row.MissingCellPolicy.CREATE_NULL_AS_BLANK
                        for (j in 0..2) {
                            val sheet = wb.getSheetAt(0)
                            val logSheet = logWB.getSheetAt(j)
                            for (i in 1..sheet.lastRowNum) {
                                sheet.getRow(i).getCell(3).stringCellValue.trim().let {
                                    logSheet.createRow(i).createCell(0)
                                        .setCellValue(it)
                                    // 新增的符卡需要增加到现有列表中
                                    if (logList.isNotEmpty() && logList[j][it] == null) {
                                        logList[j][it] =
                                            LogModel(sheet.getRow(i).getCell(1).numericCellValue.toInt(), 0, 0, 0, 0)
                                    }
                                }
                            }
                        }
                    }
                }
                logWB.write(FileOutputStream(logFile))
            }
        } catch (e: Exception) {
            logger.error("create log file failed: ", e)
        }
    }

    enum class LogType {
        APPEAR,
        SELECT,
        GET
    }

    annotation class GameType {
        companion object {
            const val NORMAL = 0
            const val BP = 1
            const val LINK = 2
            fun getGameType(roomType: RoomType): Int {
                return when (roomType) {
                    is RoomTypeNormal -> NORMAL
                    is RoomTypeBP -> BP
                    is RoomTypeLink -> LINK
                    is RoomTypeDualPage -> NORMAL
                }
            }
        }
    }

    data class SpellTimeStamp(val start: Long)

    private class LogModel(var id: Int, var appear: Int, var select: Int, var get: Int, var time: Long) {
        var changed: Boolean = false // 是否是脏数据
        fun addAppear(): LogModel {
            appear++
            return this
        }

        fun addSelect(): LogModel {
            select++
            return this
        }

        fun getCard(time: Long): LogModel {
            if (this.time == 0L) {
                this.time = time
            } else {
                // 重新计算平均数
                this.time = (this.time * get + time) / (get + 1)
            }

            get++
            return this
        }

        /** 不记录收卡时长 */
        fun getCard(): LogModel {
            get++
            return this
        }
    }
}
