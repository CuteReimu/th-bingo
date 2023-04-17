package org.tfcc.bingo

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.tfcc.bingo.message.HandlerException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 记录每张卡的出现次数，选择次数，收取次数，平均收取时长
 * 每隔一分钟存储一次数据
 */
object SpellLog {
    // 等待被存储的log
    private val logList = HashMap<String, LogModel>()

    // 收卡时间记录
    private val timeLogs = HashMap<String, SpellTimeStamp>()

    init {
        readFile()
    }

    fun logRandSpells(cards: Array<Spell>) {
        for (card in cards) {
            logSpell(LogType.APPEAR, card)
        }
    }

    fun logSpellOperate(status: SpellStatus, spell: Spell, token: String, time: Long = System.currentTimeMillis()) {
        if (status.isSelectStatus()) {
            logSpell(LogType.SELECT, spell, token, time)
        }
        if (status.isGetStatus()) {
            logSpell(LogType.GET, spell, token)
        }
    }

    // 一场比赛结束时存储，此时没收掉的卡就无视
    fun saveFile() {
        timeLogs.clear()
        val file = File("log.xlsx")
        if (!file.exists()) file.createNewFile()
        val wb = XSSFWorkbook(FileInputStream(file))
        val sheet = wb.getSheetAt(0)
        for (model in logList) {
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
        wb.write(FileOutputStream(file))
        wb.close()
    }

    private fun logSpell(type: LogType, card: Spell, token: String = "", time: Long = System.currentTimeMillis()) {
        when (type) {
            LogType.APPEAR -> {
                logList[card.name]?.let {
                    logList.put(card.name, it.addAppear())
                }
            }

            LogType.SELECT -> {
                logList[card.name] = logList[card.name]!!.addSelect()
                timeLogs[token] = SpellTimeStamp(time)
            }

            LogType.GET -> {
                timeLogs[token]?.let {
                    logList[card.name] = logList[card.name]!!.getCard(time - it.start)
                }
                timeLogs.remove(token)
            }
        }
        logList[card.name]!!.changed = true
    }

    private fun readFile() {
        val file = File("log.xlsx")
        if (!file.exists()) {
            createLogFile()
        }
        val wb = XSSFWorkbook(FileInputStream(file))
        val sheet = wb.getSheetAt(0)
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i)
            logList[row.getCell(0).stringCellValue.trim()] = LogModel(
                id = i,
                appear = row.getCell(1)?.numericCellValue?.toInt() ?: 0,
                select = row.getCell(2)?.numericCellValue?.toInt() ?: 0,
                get = row.getCell(3)?.numericCellValue?.toInt() ?: 0,
                time = row.getCell(4)?.numericCellValue?.toLong() ?: 0,
            )
        }
    }

    // 根据已有的文件生产log文件
    private fun createLogFile() {
        val logFile = File("log.xlsx")
        val logWB = XSSFWorkbook()
        val logSheet = logWB.createSheet()
        val files = File(".").listFiles() ?: throw HandlerException("找不到符卡文件")
        for (file in files) {
            if (file.extension != "xlsx" || file.name.startsWith("log")) continue
            val wb = XSSFWorkbook(FileInputStream(file))
            val sheet = wb.getSheetAt(0)
            for (i in 1..sheet.lastRowNum) {
                logSheet.createRow(i).createCell(0).setCellValue(sheet.getRow(i).getCell(3).stringCellValue.trim())
            }
        }
        logWB.write(FileOutputStream(logFile))
        logWB.close()
    }

    enum class LogType {
        APPEAR, SELECT, GET
    }

    data class SpellTimeStamp(val start: Long)

    class LogModel(var id: Int, var appear: Int, var select: Int, var get: Int, var time: Long) {
        var changed: Boolean = false
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
    }
}