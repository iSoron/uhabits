/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.io

import org.isoron.uhabits.core.database.Cursor
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.DatabaseOpener
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.core.utils.isSQLite3File
import java.io.File
import javax.inject.Inject

/**
 * Class that imports database files exported by Rewire.
 */
class RewireDBImporter
@Inject constructor(
    private val habitList: HabitList,
    private val modelFactory: ModelFactory,
    private val opener: DatabaseOpener
) : AbstractImporter() {

    override fun canHandle(file: File): Boolean {
        if (!file.isSQLite3File()) return false
        val db = opener.open(file)
        val c = db.query(
            "select count(*) from SQLITE_MASTER " +
                "where name='CHECKINS' or name='UNIT'"
        )
        val result = c.moveToNext() && c.getInt(0) == 2
        c.close()
        db.close()
        return result
    }

    override fun importHabitsFromFile(file: File) {
        val db = opener.open(file)
        db.beginTransaction()
        createHabits(db)
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    private fun createHabits(db: Database) {
        var c: Cursor? = null
        try {
            c = db.query(
                "select _id, name, description, schedule, " +
                    "active_days, repeating_count, days, period " +
                    "from habits"
            )
            if (!c.moveToNext()) return
            do {
                val id = c.getInt(0)!!
                val name = c.getString(1)
                val description = c.getString(2)
                val schedule = c.getInt(3)!!
                val activeDays = c.getString(4)
                val repeatingCount = c.getInt(5)!!
                val days = c.getInt(6)!!
                val periodIndex = c.getInt(7)!!

                val habit = modelFactory.buildHabit()
                habit.name = name!!
                habit.description = description ?: ""
                val periods = intArrayOf(7, 31, 365)
                var numerator: Int
                var denominator: Int
                when (schedule) {
                    0 -> {
                        numerator = activeDays!!.split(",").toTypedArray().size
                        denominator = 7
                    }
                    1 -> {
                        numerator = days
                        denominator = periods[periodIndex]
                    }
                    2 -> {
                        numerator = 1
                        denominator = repeatingCount
                    }
                    else -> throw IllegalStateException()
                }
                habit.frequency = Frequency(numerator, denominator)
                habitList.add(habit)
                createReminder(db, habit, id)
                createCheckmarks(db, habit, id)
            } while (c.moveToNext())
        } finally {
            c?.close()
        }
    }

    private fun createCheckmarks(
        db: Database,
        habit: Habit,
        rewireHabitId: Int
    ) {
        var c: Cursor? = null
        try {
            c = db.query(
                "select distinct date from checkins where habit_id=? and type=2",
                rewireHabitId.toString(),
            )
            if (!c.moveToNext()) return
            do {
                val date = c.getString(0)
                val year = date!!.substring(0, 4).toInt()
                val month = date.substring(4, 6).toInt()
                val day = date.substring(6, 8).toInt()
                val cal = DateUtils.getStartOfTodayCalendar()
                cal[year, month - 1] = day
                habit.originalEntries.add(Entry(Timestamp(cal), Entry.YES_MANUAL))
            } while (c.moveToNext())
        } finally {
            c?.close()
        }
    }

    private fun createReminder(db: Database, habit: Habit, rewireHabitId: Int) {
        var c: Cursor? = null
        try {
            c = db.query(
                "select time, active_days from reminders where habit_id=? limit 1",
                rewireHabitId.toString(),
            )
            if (!c.moveToNext()) return
            val rewireReminder = c.getInt(0)!!
            if (rewireReminder <= 0 || rewireReminder >= 1440) return
            val reminderDays = BooleanArray(7)
            val activeDays = c.getString(1)!!.split(",").toTypedArray()
            for (d in activeDays) {
                val idx = (d.toInt() + 1) % 7
                reminderDays[idx] = true
            }
            val hour = rewireReminder / 60
            val minute = rewireReminder % 60
            val days = WeekdayList(reminderDays)
            val reminder = Reminder(hour, minute, days)
            habit.reminder = reminder
            habitList.update(habit)
        } finally {
            c?.close()
        }
    }
}
