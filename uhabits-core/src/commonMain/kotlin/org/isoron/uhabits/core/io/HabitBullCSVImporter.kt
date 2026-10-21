/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import me.tatarka.inject.annotations.Inject
import org.isoron.platform.io.UserFile
import org.isoron.platform.io.parseCsvLine
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.ModelFactory

/**
 * Class that imports data from HabitBull CSV files.
 */
@Inject
class HabitBullCSVImporter(
    private val habitList: HabitList,
    private val modelFactory: ModelFactory,
    logging: Logging
) : AbstractImporter() {

    private val logger = logging.getLogger("HabitBullCSVImporter")

    override suspend fun canHandle(file: UserFile): Boolean {
        return try {
            val lines = file.lines()
            if (lines.isEmpty()) return false
            lines[0].startsWith("HabitName,HabitDescription,HabitCategory")
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun importHabitsFromFile(file: UserFile) {
        val lines = file.lines()
        val map = HashMap<String, Habit>()
        for (line in lines) {
            val cols = parseCsvLine(line)
            if (cols.size < 6) continue
            val name = cols[0]
            if (name == "HabitName") continue
            val description = cols[1]
            val date = parseDate(cols[3])
            var h = map[name]
            if (h == null) {
                h = modelFactory.buildHabit()
                h.name = name
                h.description = description
                h.category = cols[2].trim()
                h.frequency = Frequency.DAILY
                habitList.add(h)
                map[name] = h
                logger.info("Creating habit: $name")
            }
            val notes = cols[5]
            when (val value = parseInt(cols[4])) {
                0 -> h.originalEntries.add(Entry(date, Entry.NO, notes))
                1 -> h.originalEntries.add(Entry(date, Entry.YES_MANUAL, notes))
                else -> {
                    if (value > 1 && h.type != HabitType.NUMERICAL) {
                        logger.info("Found a value of $value, considering this habit as numerical.")
                        h.type = HabitType.NUMERICAL
                    }
                    h.originalEntries.add(Entry(date, value * 1000, notes))
                }
            }
        }

        map.forEach { (_, habit) -> habit.recompute() }
    }

    private fun parseDate(rawValue: String): LocalDate {
        if (rawValue.contains("-")) {
            val parts = rawValue.split("-")
            return LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
        if (rawValue.contains("/")) {
            val parts = rawValue.split("/")
            return LocalDate(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())
        }
        throw Exception("Unrecognized date format: $rawValue")
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
