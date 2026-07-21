package org.isoron.uhabits.core.database

import org.isoron.platform.io.Database
import org.isoron.platform.io.PreparedStatement
import org.isoron.platform.io.StepResult
import org.isoron.platform.io.queryLong
import org.isoron.platform.io.run

data class HabitData(
    var id: Long? = null,
    var name: String = "",
    var description: String = "",
    var question: String = "",
    var freqNum: Int = 1,
    var freqDen: Int = 1,
    var freqMode: Int = 0,
    var color: Int = 0,
    var position: Int = 0,
    var reminderHour: Int? = null,
    var reminderMin: Int? = null,
    var reminderDays: Int = 0,
    var highlight: Int = 0,
    var archived: Int = 0,
    var type: Int = 0,
    var targetValue: Double = 0.0,
    var targetType: Int = 0,
    var unit: String = "",
    var uuid: String? = null
)

class HabitRepository(private val db: Database) {
    private val findAllStmt by lazy {
        db.prepareStatement(
            """SELECT id, name, description, question, freq_num, freq_den, freq_mode, color,
               position, reminder_hour, reminder_min, reminder_days, highlight,
               archived, type, target_value, target_type, unit, uuid
               FROM Habits ORDER BY position"""
        )
    }

    private val insertStmt by lazy {
        db.prepareStatement(
            """INSERT INTO Habits(name, description, question, freq_num, freq_den, freq_mode,
               color, position, reminder_hour, reminder_min, reminder_days,
               highlight, archived, type, target_value, target_type, unit, uuid)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
        )
    }

    private val insertWithIdStmt by lazy {
        db.prepareStatement(
            """INSERT INTO Habits(id, name, description, question, freq_num, freq_den, freq_mode,
               color, position, reminder_hour, reminder_min, reminder_days,
               highlight, archived, type, target_value, target_type, unit, uuid)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
        )
    }

    private val updateStmt by lazy {
        db.prepareStatement(
            """UPDATE Habits SET name=?, description=?, question=?, freq_num=?,
               freq_den=?, freq_mode=?, color=?, position=?, reminder_hour=?, reminder_min=?,
               reminder_days=?, highlight=?, archived=?, type=?, target_value=?,
               target_type=?, unit=?, uuid=? WHERE id=?"""
        )
    }

    private val deleteStmt by lazy {
        db.prepareStatement("DELETE FROM Habits WHERE id = ?")
    }

    fun findAll(): List<HabitData> {
        findAllStmt.reset()
        val results = mutableListOf<HabitData>()
        while (findAllStmt.step() == StepResult.ROW) {
            results.add(readRow(findAllStmt))
        }
        return results
    }

    fun insert(data: HabitData): Long {
        if (data.id != null) {
            insertWithIdStmt.reset()
            insertWithIdStmt.bindLong(1, data.id!!)
            bindForInsert(insertWithIdStmt, data, offset = 1)
            insertWithIdStmt.step()
            return data.id!!
        }
        insertStmt.reset()
        bindForInsert(insertStmt, data)
        insertStmt.step()
        return db.queryLong("SELECT last_insert_rowid()")
    }

    fun update(data: HabitData) {
        updateStmt.reset()
        bindForInsert(updateStmt, data)
        updateStmt.bindLong(19, data.id!!)
        updateStmt.step()
    }

    fun delete(id: Long) {
        deleteStmt.reset()
        deleteStmt.bindLong(1, id)
        deleteStmt.step()
    }

    fun execSQL(sql: String) = db.run(sql)

    fun execSQL(sql: String, bind: PreparedStatement.() -> Unit) = db.run(sql, bind)

    private fun bindForInsert(stmt: PreparedStatement, data: HabitData, offset: Int = 0) {
        val o = offset
        stmt.bindText(1 + o, data.name)
        stmt.bindText(2 + o, data.description)
        stmt.bindText(3 + o, data.question)
        stmt.bindInt(4 + o, data.freqNum)
        stmt.bindInt(5 + o, data.freqDen)
        stmt.bindInt(6 + o, data.freqMode)
        stmt.bindInt(7 + o, data.color)
        stmt.bindInt(8 + o, data.position)
        if (data.reminderHour != null) stmt.bindInt(9 + o, data.reminderHour!!) else stmt.bindNull(9 + o)
        if (data.reminderMin != null) stmt.bindInt(10 + o, data.reminderMin!!) else stmt.bindNull(10 + o)
        stmt.bindInt(11 + o, data.reminderDays)
        stmt.bindInt(12 + o, data.highlight)
        stmt.bindInt(13 + o, data.archived)
        stmt.bindInt(14 + o, data.type)
        stmt.bindReal(15 + o, data.targetValue)
        stmt.bindInt(16 + o, data.targetType)
        stmt.bindText(17 + o, data.unit)
        if (data.uuid != null) stmt.bindText(18 + o, data.uuid!!) else stmt.bindNull(18 + o)
    }

    private fun readRow(stmt: PreparedStatement): HabitData {
        return HabitData(
            id = stmt.getLong(0),
            name = stmt.getText(1),
            description = stmt.getText(2),
            question = stmt.getText(3),
            freqNum = stmt.getInt(4),
            freqDen = stmt.getInt(5),
            freqMode = stmt.getInt(6),
            color = stmt.getInt(7),
            position = stmt.getInt(8),
            reminderHour = stmt.getIntOrNull(9),
            reminderMin = stmt.getIntOrNull(10),
            reminderDays = stmt.getInt(11),
            highlight = stmt.getInt(12),
            archived = stmt.getInt(13),
            type = stmt.getInt(14),
            targetValue = stmt.getReal(15),
            targetType = stmt.getInt(16),
            unit = stmt.getText(17),
            uuid = stmt.getTextOrNull(18)
        )
    }
}
