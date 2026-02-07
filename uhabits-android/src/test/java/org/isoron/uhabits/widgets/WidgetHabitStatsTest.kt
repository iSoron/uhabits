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
package org.isoron.uhabits.widgets

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.models.memory.MemoryModelFactory
import org.isoron.uhabits.core.utils.DateUtils
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class WidgetHabitStatsTest {

    private val gmt = TimeZone.getTimeZone("GMT")

    @Before
    fun setUp() {
        DateUtils.setFixedTimeZone(gmt)
        DateUtils.setFixedLocalTime(null)
        DateUtils.setStartDayOffset(0, 0)
    }

    @After
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
        DateUtils.setFixedLocalTime(null)
        DateUtils.setStartDayOffset(0, 0)
    }

    @Test
    fun computeCurrentStreakDays_usesYesterdayWhenTodayIsUnknown() {
        val habit = MemoryModelFactory().buildHabit()
        val today = ts(2026, Calendar.FEBRUARY, 7)

        habit.computedEntries.add(Entry(today.minus(1), Entry.YES_MANUAL))
        habit.computedEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))

        assertEquals(2, WidgetHabitStats.computeCurrentStreakDays(habit, today))
    }

    @Test
    fun computeCurrentStreakDays_isZeroWhenTodayIsExplicitlyNo() {
        val habit = MemoryModelFactory().buildHabit()
        val today = ts(2026, Calendar.FEBRUARY, 7)

        habit.computedEntries.add(Entry(today, Entry.NO))
        habit.computedEntries.add(Entry(today.minus(1), Entry.YES_MANUAL))
        habit.computedEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))

        assertEquals(0, WidgetHabitStats.computeCurrentStreakDays(habit, today))
    }

    @Test
    fun computeWeeklySuccess_returnsOldestToNewest() {
        val habit = MemoryModelFactory().buildHabit()
        val today = ts(2026, Calendar.FEBRUARY, 7)

        habit.computedEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))
        habit.computedEntries.add(Entry(today, Entry.YES_MANUAL))

        val expected = booleanArrayOf(false, false, false, false, true, false, true)
        assertArrayEquals(expected, WidgetHabitStats.computeWeeklySuccess(habit, today))
    }

    @Test
    fun computeNextReminderTimeUtcMillis_respectsWeekdays() {
        val habit = MemoryModelFactory().buildHabit()
        habit.reminder = Reminder(19, 30, WeekdayList(4)) // Monday only

        val now = utcMillis(2026, Calendar.FEBRUARY, 7, 18, 0) // Saturday
        val expected = utcMillis(2026, Calendar.FEBRUARY, 9, 19, 30) // Monday

        assertEquals(expected, WidgetHabitStats.computeNextReminderTimeUtcMillis(habit, now))
    }

    @Test
    fun computeNextReminderTimeUtcMillis_usesTomorrowWhenTimeAlreadyPassed() {
        val habit = MemoryModelFactory().buildHabit()
        habit.reminder = Reminder(19, 30, WeekdayList.EVERY_DAY)

        val now = utcMillis(2026, Calendar.FEBRUARY, 7, 20, 0)
        val expected = utcMillis(2026, Calendar.FEBRUARY, 8, 19, 30)

        assertEquals(expected, WidgetHabitStats.computeNextReminderTimeUtcMillis(habit, now))
    }

    @Test
    fun computeNextReminderTimeUtcMillis_appliesStartOfDayOffsetForWeekdays() {
        DateUtils.setStartDayOffset(4, 0)

        val habit = MemoryModelFactory().buildHabit()
        habit.reminder = Reminder(2, 0, WeekdayList(4)) // Monday only

        val now = utcMillis(2026, Calendar.FEBRUARY, 9, 0, 30) // Monday
        val expected = utcMillis(2026, Calendar.FEBRUARY, 10, 2, 0) // Tuesday 02:00 counts as Monday

        assertEquals(expected, WidgetHabitStats.computeNextReminderTimeUtcMillis(habit, now))
    }

    private fun ts(year: Int, javaMonth: Int, day: Int): Timestamp {
        val cal = GregorianCalendar(gmt)
        cal.set(year, javaMonth, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Timestamp(cal.timeInMillis)
    }

    private fun utcMillis(year: Int, javaMonth: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = GregorianCalendar(gmt)
        cal.set(year, javaMonth, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
