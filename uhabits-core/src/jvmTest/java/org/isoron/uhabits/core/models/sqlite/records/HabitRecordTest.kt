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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.junit.Test

class HabitRecordTest : BaseUnitTest() {
    @Test
    fun testCopyRestore1() {
        val original = modelFactory.buildHabit()
        original.name = "Hello world"
        original.question = "Did you greet the world today?"
        original.color = PaletteColor(1)
        original.isArchived = true
        original.frequency = Frequency.THREE_TIMES_PER_WEEK
        original.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        original.id = 1000L
        original.position = 20
        val record = HabitRecord()
        record.copyFrom(original)
        val duplicate = modelFactory.buildHabit()
        record.copyTo(duplicate)
        assertThat(original, IsEqual.equalTo(duplicate))
    }

    @Test
    fun testCopyRestore2() {
        val original = modelFactory.buildHabit()
        original.name = "Hello world"
        original.question = "Did you greet the world today?"
        original.color = PaletteColor(5)
        original.isArchived = false
        original.frequency = Frequency.DAILY
        original.reminder = null
        original.id = 1L
        original.position = 15
        original.type = Habit.NUMBER_HABIT
        original.targetValue = 100.0
        original.targetType = Habit.AT_LEAST
        original.unit = "miles"
        val record = HabitRecord()
        record.copyFrom(original)
        val duplicate = modelFactory.buildHabit()
        record.copyTo(duplicate)
        assertThat(original, IsEqual.equalTo(duplicate))
    }
}
