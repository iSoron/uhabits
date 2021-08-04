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
package org.isoron.uhabits.core.models.sqlite.records

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.junit.Test

class HabitRecordTest : BaseUnitTest() {
    @Test
    fun testCopyRestore1() {
        val original = modelFactory.buildHabit().apply {
            name = "Hello world"
            question = "Did you greet the world today?"
            color = PaletteColor(1)
            isArchived = true
            frequency = Frequency.THREE_TIMES_PER_WEEK
            reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
            id = 1000L
            position = 20
        }
        val record = HabitRecord()
        record.copyFrom(original)
        val duplicate = modelFactory.buildHabit()
        record.copyTo(duplicate)
        assertThat(original, equalTo(duplicate))
    }

    @Test
    fun testCopyRestore2() {
        val original = modelFactory.buildHabit().apply {
            name = "Hello world"
            question = "Did you greet the world today?"
            color = PaletteColor(5)
            isArchived = false
            frequency = Frequency.DAILY
            reminder = null
            id = 1L
            position = 15
            type = HabitType.NUMERICAL
            targetValue = 100.0
            targetType = NumericalHabitType.AT_LEAST
            unit = "miles"
        }
        val record = HabitRecord()
        record.copyFrom(original)
        val duplicate = modelFactory.buildHabit()
        record.copyTo(duplicate)
        assertThat(original, equalTo(duplicate))
    }
}
