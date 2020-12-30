/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import com.opencsv.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.utils.*
import java.io.*
import java.util.*
import javax.inject.*

/**
 * Class that imports data from HabitBull CSV files.
 */
class HabitBullCSVImporter
@Inject constructor(
        habits: HabitList,
        private val modelFactory: ModelFactory,
) : AbstractImporter(habits) {

    override fun canHandle(file: File): Boolean {
        val reader = BufferedReader(FileReader(file))
        val line = reader.readLine()
        return line.startsWith("HabitName,HabitDescription,HabitCategory")
    }

    override fun importHabitsFromFile(file: File) {
        val reader = CSVReader(FileReader(file))
        val map = HashMap<String, Habit>()
        for (line in reader) {
            val name = line[0]
            if (name == "HabitName") continue
            val description = line[1]
            val dateString = line[3].split("-").toTypedArray()
            val year = dateString[0].toInt()
            val month = dateString[1].toInt()
            val day = dateString[2].toInt()
            val date = DateUtils.getStartOfTodayCalendar()
            date[year, month - 1] = day
            val timestamp = Timestamp(date.timeInMillis)
            val value = line[4].toInt()
            if (value != 1) continue
            var h = map[name]
            if (h == null) {
                h = modelFactory.buildHabit()
                h.name = name
                h.description = description ?: ""
                h.frequency = Frequency.DAILY
                habitList.add(h)
                map[name] = h
            }
            h.originalEntries.add(Entry(timestamp, Entry.YES_MANUAL))
        }
    }
}