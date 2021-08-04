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
package org.isoron.uhabits

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Frequency.Companion.DAILY
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday

class HabitFixtures(private val modelFactory: ModelFactory, private val habitList: HabitList) {
    var LONG_HABIT_ENTRIES = booleanArrayOf(
        true, false, false, true, true, true, false, false, true, true
    )
    var LONG_NUMERICAL_HABIT_ENTRIES = intArrayOf(
        200000, 0, 150000, 137000, 0, 0, 500000, 30000, 100000, 0, 300000,
        100000, 0, 100000
    )

    fun createEmptyHabit(): Habit {
        val habit = modelFactory.buildHabit()
        habit.name = "Meditate"
        habit.question = "Did you meditate this morning?"
        habit.description = "This is a test description"
        habit.color = PaletteColor(5)
        habit.frequency = DAILY
        habitList.add(habit)
        return habit
    }

    fun createLongHabit(): Habit {
        val habit = createEmptyHabit()
        habit.frequency = Frequency(3, 7)
        habit.color = PaletteColor(7)
        val today: Timestamp = getToday()
        val marks = intArrayOf(
            0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27,
            28, 50, 51, 52, 53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80,
            81, 83, 89, 90, 91, 95, 102, 103, 108, 109, 120
        )
        for (mark in marks) habit.originalEntries.add(Entry(today.minus(mark), YES_MANUAL))
        habit.recompute()
        return habit
    }

    fun createVeryLongHabit(): Habit {
        val habit = createEmptyHabit()
        habit.frequency = Frequency(1, 2)
        habit.color = PaletteColor(11)
        val today: Timestamp = getToday()
        val marks = intArrayOf(
            0, 3, 5, 6, 7, 10, 13, 14, 15, 18, 21, 22, 23, 24, 27, 28, 30, 31, 34, 37,
            39, 42, 43, 46, 47, 48, 51, 52, 54, 55, 57, 59, 62, 65, 68, 71, 73, 76, 79,
            80, 81, 83, 85, 86, 89, 90, 91, 94, 96, 98, 100, 103, 104, 106, 109, 111,
            112, 113, 115, 117, 120, 123, 126, 129, 132, 134, 136, 139, 141, 142, 145,
            148, 149, 151, 152, 154, 156, 157, 159, 161, 162, 163, 164, 166, 168, 170,
            172, 173, 174, 175, 176, 178, 180, 181, 184, 185, 188, 189, 190, 191, 194,
            195, 197, 198, 199, 200, 202, 205, 208, 211, 213, 215, 216, 218, 220, 222,
            223, 225, 227, 228, 230, 231, 232, 234, 235, 238, 241, 242, 244, 247, 250,
            251, 253, 254, 257, 260, 261, 263, 264, 266, 269, 272, 273, 276, 279, 281,
            284, 285, 288, 291, 292, 294, 296, 297, 299, 300, 301, 303, 306, 307, 308,
            309, 310, 313, 316, 319, 322, 324, 326, 329, 330, 332, 334, 335, 337, 338,
            341, 344, 345, 346, 347, 350, 352, 355, 358, 360, 361, 362, 363, 365, 368,
            371, 373, 374, 376, 379, 380, 382, 384, 385, 387, 389, 390, 392, 393, 395,
            396, 399, 401, 404, 407, 410, 411, 413, 414, 416, 417, 419, 420, 423, 424,
            427, 429, 431, 433, 436, 439, 440, 442, 445, 447, 450, 453, 454, 456, 459,
            460, 461, 464, 466, 468, 470, 473, 474, 475, 477, 479, 481, 482, 483, 486,
            489, 491, 493, 495, 497, 498, 500, 503, 504, 507, 510, 511, 512, 515, 518,
            519, 521, 522, 525, 528, 531, 532, 534, 537, 539, 541, 543, 544, 547, 550,
            551, 554, 556, 557, 560, 561, 564, 567, 568, 569, 570, 572, 575, 576, 579,
            582, 583, 584, 586, 589
        )
        for (mark in marks) habit.originalEntries.add(Entry(today.minus(mark), YES_MANUAL))
        habit.recompute()
        return habit
    }

    fun createLongNumericalHabit(): Habit {
        val habit = modelFactory.buildHabit().apply {
            name = "Read"
            question = "How many pages did you walk today?"
            type = HabitType.NUMERICAL
            targetType = NumericalHabitType.AT_LEAST
            targetValue = 200.0
            unit = "pages"
        }
        habitList.add(habit)
        var timestamp: Timestamp = getToday()
        for (value in LONG_NUMERICAL_HABIT_ENTRIES) {
            habit.originalEntries.add(Entry(timestamp, value))
            timestamp = timestamp.minus(1)
        }
        habit.recompute()
        return habit
    }

    fun createShortHabit(): Habit {
        val habit = modelFactory.buildHabit().apply {
            name = "Wake up early"
            question = "Did you wake up before 6am?"
            frequency = Frequency(2, 3)
        }
        habitList.add(habit)
        var timestamp: Timestamp = getToday()
        for (c in LONG_HABIT_ENTRIES) {
            if (c) habit.originalEntries.add(Entry(timestamp, YES_MANUAL))
            timestamp = timestamp.minus(1)
        }
        habit.recompute()
        return habit
    }

    @Synchronized
    fun purgeHabits(habitList: HabitList) {
        habitList.removeAll()
    }
}
