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
package org.isoron.uhabits.core.test

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.sqlite.SQLiteEntryList
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday

class HabitFixtures(private val modelFactory: ModelFactory, private val habitList: HabitList) {
    private var NON_DAILY_HABIT_CHECKS = booleanArrayOf(
        true, false, false, true, true, true, false, false, true, true
    )

    fun createEmptyHabit(
        name: String = "Meditate",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0
    ): Habit {
        val habit = modelFactory.buildHabit()
        habit.name = name
        habit.question = "Did you meditate this morning?"
        habit.color = color
        habit.position = position
        habit.frequency = Frequency.DAILY
        saveIfSQLite(habit)
        return habit
    }

    fun createEmptyNumericalHabit(targetType: NumericalHabitType): Habit {
        val habit = modelFactory.buildHabit()
        habit.type = HabitType.NUMERICAL
        habit.name = "Run"
        habit.question = "How many miles did you run today?"
        habit.unit = "miles"
        habit.targetType = targetType
        habit.targetValue = 2.0
        habit.color = PaletteColor(1)
        saveIfSQLite(habit)
        return habit
    }

    fun createLongHabit(): Habit {
        val habit = createEmptyHabit()
        habit.frequency = Frequency(3, 7)
        habit.color = PaletteColor(4)
        val today = getToday()
        val marks = intArrayOf(
            0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27,
            28, 50, 51, 52, 53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80,
            81, 83, 89, 90, 91, 95, 102, 103, 108, 109, 120
        )
        for (mark in marks) habit.originalEntries.add(Entry(today.minus(mark), Entry.YES_MANUAL))
        habit.recompute()
        return habit
    }

    fun createNumericalHabit(): Habit {
        val habit = modelFactory.buildHabit()
        habit.type = HabitType.NUMERICAL
        habit.name = "Run"
        habit.question = "How many miles did you run today?"
        habit.unit = "miles"
        habit.targetType = NumericalHabitType.AT_LEAST
        habit.targetValue = 2.0
        habit.color = PaletteColor(1)
        saveIfSQLite(habit)
        val today = getToday()
        val times = intArrayOf(0, 1, 3, 5, 7, 8, 9, 10)
        val values = intArrayOf(100, 200, 300, 400, 500, 600, 700, 800)
        for (i in times.indices) {
            val timestamp = today.minus(times[i])
            habit.originalEntries.add(Entry(timestamp, values[i]))
        }
        habit.recompute()
        return habit
    }

    fun createLongNumericalHabit(reference: Timestamp): Habit {
        val habit = modelFactory.buildHabit()
        habit.type = HabitType.NUMERICAL
        habit.name = "Walk"
        habit.question = "How many steps did you walk today?"
        habit.unit = "steps"
        habit.targetType = NumericalHabitType.AT_LEAST
        habit.targetValue = 100.0
        habit.color = PaletteColor(1)
        saveIfSQLite(habit)
        val times = intArrayOf(
            0, 5, 9, 15, 17, 21, 23, 27, 28, 35, 41, 45, 47, 53, 56, 62, 70, 73, 78,
            83, 86, 94, 101, 106, 113, 114, 120, 126, 130, 133, 141, 143, 148, 151, 157, 164,
            166, 171, 173, 176, 179, 183, 191, 259, 264, 268, 270, 275, 282, 284, 289, 295,
            302, 306, 310, 315, 323, 325, 328, 335, 343, 349, 351, 353, 357, 359, 360, 367,
            372, 376, 380, 385, 393, 400, 404, 412, 415, 418, 422, 425, 433, 437, 444, 449,
            455, 460, 462, 465, 470, 471, 479, 481, 485, 489, 494, 495, 500, 501, 503, 507
        )
        val values = intArrayOf(
            230, 306, 148, 281, 134, 285, 104, 158, 325, 236, 303, 210, 118, 124,
            301, 201, 156, 376, 347, 367, 396, 134, 160, 381, 155, 354, 231, 134, 164, 354,
            236, 398, 199, 221, 208, 397, 253, 276, 214, 341, 299, 221, 353, 250, 341, 168,
            374, 205, 182, 217, 297, 321, 104, 237, 294, 110, 136, 229, 102, 271, 250, 294,
            158, 319, 379, 126, 282, 155, 288, 159, 215, 247, 207, 226, 244, 158, 371, 219,
            272, 228, 350, 153, 356, 279, 394, 202, 213, 214, 112, 248, 139, 245, 165, 256,
            370, 187, 208, 231, 341, 312
        )
        for (i in times.indices) {
            val timestamp = reference.minus(times[i])
            habit.originalEntries.add(Entry(timestamp, values[i]))
        }
        habit.recompute()
        return habit
    }

    fun createShortHabit(): Habit {
        val habit = modelFactory.buildHabit()
        habit.name = "Wake up early"
        habit.question = "Did you wake up before 6am?"
        habit.frequency = Frequency(2, 3)
        saveIfSQLite(habit)
        var timestamp = getToday()
        for (c in NON_DAILY_HABIT_CHECKS) {
            var value = Entry.NO
            if (c) value = Entry.YES_MANUAL
            habit.originalEntries.add(Entry(timestamp, value))
            timestamp = timestamp.minus(1)
        }
        habit.recompute()
        return habit
    }

    private fun saveIfSQLite(habit: Habit) {
        if (habit.originalEntries !is SQLiteEntryList) return
        habitList.add(habit)
    }
}
