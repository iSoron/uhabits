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

import com.opencsv.CSVReader
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.Timestamp
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject

/**
 * Class that imports data from HabitBull CSV files.
 */
class HabitBullCSVImporter
@Inject constructor(
    private val habitList: HabitList,
    private val modelFactory: ModelFactory,
    logging: Logging,
) : AbstractImporter() {

    private val logger = logging.getLogger("HabitBullCSVImporter")

    override fun canHandle(file: File): Boolean {
        val reader = BufferedReader(FileReader(file))
        val line = reader.readLine()
        return line.startsWith("HabitName,HabitDescription,HabitCategory")
    }

    override fun importHabitsFromFile(file: File) {
        val reader = CSVReader(FileReader(file))
        val map = HashMap<String, Habit>()
        for (cols in reader) {
            val name = cols[0]
            if (name == "HabitName") continue
            val description = cols[1]
            val timestamp = parseTimestamp(cols[3])
            var h = map[name]
            if (h == null) {
                h = modelFactory.buildHabit()
                h.name = name
                h.description = description ?: ""
                h.frequency = Frequency.DAILY
                habitList.add(h)
                map[name] = h
                logger.info("Creating habit: $name")
            }
            val notes = cols[5] ?: ""
            when (val value = parseInt(cols[4])) {
                0 -> h.originalEntries.add(Entry(timestamp, Entry.NO, notes))
                1 -> h.originalEntries.add(Entry(timestamp, Entry.YES_MANUAL, notes))
                else -> {
                    if (value > 1 && h.type != HabitType.NUMERICAL) {
                        logger.info("Found a value of $value, considering this habit as numerical.")
                        h.type = HabitType.NUMERICAL
                    }
                    h.originalEntries.add(Entry(timestamp, value, notes))
                }
            }
        }
    }

    private fun parseTimestamp(rawValue: String): Timestamp {
        val formats = listOf(
            DateFormat.getDateInstance(DateFormat.SHORT),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
        )
        var parsedDate: Date? = null
        for (fmt in formats) {
            try {
                parsedDate = fmt.parse(rawValue)
            } catch (e: ParseException) {
                // ignored
            }
        }
        if (parsedDate == null) {
            throw Exception("Unrecognized date format: $rawValue")
        }
        val parsedCalendar = GregorianCalendar()
        parsedCalendar.time = parsedDate
        return Timestamp.from(
            parsedCalendar[YEAR],
            parsedCalendar[MONTH],
            parsedCalendar[DAY_OF_MONTH],
        )
    }

    private fun parseInt(rawValue: String): Int {
        return try {
            rawValue.toInt()
        } catch (e: NumberFormatException) {
            logger.error("Could not parse int: $rawValue. Replacing by zero.")
            0
        }
    }
}
