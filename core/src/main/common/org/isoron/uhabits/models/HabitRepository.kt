/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.models

import org.isoron.platform.gui.*
import org.isoron.platform.io.Database
import org.isoron.platform.io.PreparedStatement
import org.isoron.platform.io.StepResult
import org.isoron.platform.io.nextId

class HabitRepository(var db: Database) {

    companion object {
        const val SELECT_COLUMNS = "id, name, description, freq_num, freq_den, color, archived, position, unit, target_value, type"
        const val SELECT_PLACEHOLDERS = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
        const val UPDATE_COLUMNS = "id=?, name=?, description=?, freq_num=?, freq_den=?, color=?, archived=?, position=?, unit=?, target_value=?, type=?"
    }

    private val findAllStatement = db.prepareStatement("select $SELECT_COLUMNS from habits order by position")
    private val insertStatement = db.prepareStatement("insert into Habits($SELECT_COLUMNS) values ($SELECT_PLACEHOLDERS)")
    private val updateStatement = db.prepareStatement("update Habits set $UPDATE_COLUMNS where id=?")
    private val deleteStatement = db.prepareStatement("delete from Habits where id=?")

    fun nextId(): Int {
        return db.nextId("Habits")
    }

    fun findAll(): MutableMap<Int, Habit> {
        val result = mutableMapOf<Int, Habit>()
        while (findAllStatement.step() == StepResult.ROW) {
            val habit = buildHabitFromStatement(findAllStatement)
            result[habit.id] = habit
        }
        findAllStatement.reset()
        return result
    }

    fun insert(habit: Habit) {
        bindHabitToStatement(habit, insertStatement)
        insertStatement.step()
        insertStatement.reset()
    }

    fun update(habit: Habit) {
        bindHabitToStatement(habit, updateStatement)
        updateStatement.bindInt(11, habit.id)
        updateStatement.step()
        updateStatement.reset()
    }

    private fun buildHabitFromStatement(stmt: PreparedStatement): Habit {
        return Habit(id = stmt.getInt(0),
                     name = stmt.getText(1),
                     description = stmt.getText(2),
                     frequency = Frequency(stmt.getInt(3), stmt.getInt(4)),
                     color = PaletteColor(stmt.getInt(5)),
                     isArchived = stmt.getInt(6) != 0,
                     position = stmt.getInt(7),
                     unit = stmt.getText(8),
                     target = stmt.getReal(9),
                     type = if (stmt.getInt(10) == 0) HabitType.BOOLEAN_HABIT else HabitType.NUMERICAL_HABIT)
    }

    private fun bindHabitToStatement(habit: Habit, statement: PreparedStatement) {
        statement.bindInt(0, habit.id)
        statement.bindText(1, habit.name)
        statement.bindText(2, habit.description)
        statement.bindInt(3, habit.frequency.numerator)
        statement.bindInt(4, habit.frequency.denominator)
        statement.bindInt(5, habit.color.index)
        statement.bindInt(6, if (habit.isArchived) 1 else 0)
        statement.bindInt(7, habit.position)
        statement.bindText(8, habit.unit)
        statement.bindReal(9, habit.target)
        statement.bindInt(10, habit.type.code)
    }

    fun delete(habit: Habit) {
        deleteStatement.bindInt(0, habit.id)
        deleteStatement.step()
        deleteStatement.reset()
    }
}