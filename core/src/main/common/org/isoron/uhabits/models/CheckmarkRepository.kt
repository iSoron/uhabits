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

import org.isoron.platform.io.*
import org.isoron.platform.time.*

class CheckmarkRepository(db: Database) {

    private val findStatement = db.prepareStatement("select timestamp, value from Repetitions where habit = ? order by timestamp desc")
    private val insertStatement = db.prepareStatement("insert into Repetitions(habit, timestamp, value) values (?, ?, ?)")
    private val deleteStatement = db.prepareStatement("delete from Repetitions where habit=? and timestamp=?")

    fun findAll(habitId: Int): List<Checkmark> {
        findStatement.bindInt(0, habitId)
        val result = mutableListOf<Checkmark>()
        while (findStatement.step() == StepResult.ROW) {
            val date = Timestamp(findStatement.getLong(0)).localDate
            val value = findStatement.getInt(1)
            result.add(Checkmark(date, value))
        }
        findStatement.reset()
        return result
    }

    fun insert(habitId: Int, checkmark: Checkmark) {
        val timestamp = checkmark.date.timestamp
        insertStatement.bindInt(0, habitId)
        insertStatement.bindLong(1, timestamp.millisSince1970)
        insertStatement.bindInt(2, checkmark.value)
        insertStatement.step()
        insertStatement.reset()
    }

    fun delete(habitId: Int, date: LocalDate) {
        val timestamp = date.timestamp
        deleteStatement.bindInt(0, habitId)
        deleteStatement.bindLong(1, timestamp.millisSince1970)
        deleteStatement.step()
        deleteStatement.reset()
    }
}