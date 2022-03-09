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
package org.isoron.uhabits.core.reminders

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.applyTimezone
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfTodayCalendar
import org.isoron.uhabits.core.utils.DateUtils.Companion.removeTimezone
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocalTime
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.junit.MockitoJUnitRunner
import java.util.Calendar
import java.util.TimeZone

@RunWith(MockitoJUnitRunner::class)
class ReminderSchedulerTest : BaseUnitTest() {
    private val habitId = 10L
    private lateinit var habit: Habit
    private lateinit var reminderScheduler: ReminderScheduler

    private val sys: ReminderScheduler.SystemScheduler = mock()
    private val widgetPreferences: WidgetPreferences = mock()

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyHabit()
        habit.id = habitId
        reminderScheduler =
            ReminderScheduler(commandRunner, habitList, sys, widgetPreferences)
        setFixedTimeZone(TimeZone.getTimeZone("GMT-4"))
    }

    @Test
    fun testScheduleAll() {
        val now = unixTime(2015, 1, 26, 13, 0)
        setFixedLocalTime(now)
        val h1 = fixtures.createEmptyHabit()
        val h2 = fixtures.createEmptyHabit()
        val h3 = fixtures.createEmptyHabit()
        h1.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        h2.reminder = Reminder(18, 30, WeekdayList.EVERY_DAY)
        h3.reminder = null
        habitList.add(h1)
        habitList.add(h2)
        habitList.add(h3)
        reminderScheduler.scheduleAll()
        verify(sys).scheduleShowReminder(
            eq(unixTime(2015, 1, 27, 12, 30)),
            eq(h1),
            anyLong()
        )
        verify(sys).scheduleShowReminder(
            eq(unixTime(2015, 1, 26, 22, 30)),
            eq(h2),
            anyLong()
        )
    }

    @Test
    fun testSchedule_atSpecificTime() {
        val atTime = unixTime(2015, 1, 30, 11, 30)
        val expectedCheckmarkTime = unixTime(2015, 1, 30, 0, 0)
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        scheduleAndVerify(atTime, expectedCheckmarkTime, atTime)
    }

    @Test
    fun testSchedule_withSnooze() {
        val now = removeTimezone(unixTime(2015, 1, 1, 15, 0))
        setFixedLocalTime(now)
        val snoozeTimeInFuture = unixTime(2015, 1, 1, 21, 0)
        val snoozeTimeInPast = unixTime(2015, 1, 1, 7, 0)
        val regularReminderTime = applyTimezone(unixTime(2015, 1, 2, 8, 30))
        val todayCheckmarkTime = unixTime(2015, 1, 1, 0, 0)
        val tomorrowCheckmarkTime = unixTime(2015, 1, 2, 0, 0)
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        whenever(widgetPreferences.getSnoozeTime(habitId)).thenReturn(snoozeTimeInFuture)
        reminderScheduler.schedule(habit)
        verify(sys).scheduleShowReminder(snoozeTimeInFuture, habit, todayCheckmarkTime)
        whenever(widgetPreferences.getSnoozeTime(habitId)).thenReturn(snoozeTimeInPast)
        reminderScheduler.schedule(habit)
        verify(sys)
            .scheduleShowReminder(regularReminderTime, habit, tomorrowCheckmarkTime)
    }

    @Test
    fun testSchedule_laterToday() {
        val now = unixTime(2015, 1, 26, 6, 30)
        setFixedLocalTime(now)
        val expectedCheckmarkTime = unixTime(2015, 1, 26, 0, 0)
        val expectedReminderTime = unixTime(2015, 1, 26, 12, 30)
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime)
    }

    @Test
    fun testSchedule_tomorrow() {
        val now = unixTime(2015, 1, 26, 13, 0)
        setFixedLocalTime(now)
        val expectedCheckmarkTime = unixTime(2015, 1, 27, 0, 0)
        val expectedReminderTime = unixTime(2015, 1, 27, 12, 30)
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime)
    }

    @Test
    fun testSchedule_withoutReminder() {
        reminderScheduler.schedule(habit)
    }

    override fun unixTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, milliseconds: Long): Long {
        val cal: Calendar = getStartOfTodayCalendar()
        cal[year, month, day, hour] = minute
        return cal.timeInMillis
    }

    private fun scheduleAndVerify(
        atTime: Long?,
        expectedCheckmarkTime: Long,
        expectedReminderTime: Long
    ) {
        if (atTime == null) reminderScheduler.schedule(habit) else reminderScheduler.scheduleAtTime(
            habit,
            atTime
        )
        verify(sys).scheduleShowReminder(
            expectedReminderTime,
            habit,
            expectedCheckmarkTime
        )
    }
}
