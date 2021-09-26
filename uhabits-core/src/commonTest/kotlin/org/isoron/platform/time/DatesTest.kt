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

package org.isoron.platform.time

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.isoron.platform.time.LocalDate.Companion.getStartOfDay
import org.isoron.platform.time.LocalDate.Companion.getStartOfDayWithOffset
import org.isoron.platform.time.LocalDate.Companion.getStartOfToday
import org.isoron.platform.time.LocalDate.Companion.getStartOfTodayWithOffset
import org.isoron.platform.time.LocalDate.Companion.getWeekdaySequence
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DatesTest {
    @Test
    fun testDatesBefore2000() {
        val date = LocalDate(-1)
        assertEquals(31, date.day)
        assertEquals(12, date.month)
        assertEquals(1999, date.year)
    }

    @Test
    fun testGetLocalTime() {
        LocalDate.fixedLocalTime = null
        LocalDate.fixedTimeZone = TimeZone.of("Australia/Sydney")
        val utcTestTimeInMillis = unixTime(2015, 1, 11)
        val localTimeInMillis = LocalDate.getLocalTime(utcTestTimeInMillis)
        val expectedUnixTimeOffsetForSydney = 11 * 60 * 60 * 1000
        val expectedUnixTimeForSydney = utcTestTimeInMillis + expectedUnixTimeOffsetForSydney
        assertEquals(expectedUnixTimeForSydney, localTimeInMillis)
    }

    @Test
    fun testGetWeekdaySequence() {
        val weekdaySequence = getWeekdaySequence(3)
        assertContentEquals(arrayOf(3, 4, 5, 6, 7, 1, 2), weekdaySequence)
    }

    @Test
    fun testGetStartOfDay() {
        val expectedStartOfDayUtc = unixTime(2017, 1, 1)
        val laterInTheDayUtc = unixTime(2017, 1, 1, 20, 0)
        val startOfDay = getStartOfDay(laterInTheDayUtc)
        assertEquals(expectedStartOfDayUtc, startOfDay)
    }

    @Test
    fun testGetStartOfToday() {
        val expectedStartOfDayUtc = unixTime(2017, 1, 1)
        val laterInTheDayUtc = unixTime(2017, 1, 1, 20, 0)
        LocalDate.fixedLocalTime = laterInTheDayUtc
        val startOfToday = getStartOfToday()
        assertEquals(expectedStartOfDayUtc, startOfToday)
    }

    @Test
    @Throws(Exception::class)
    fun testGetStartOfDayWithOffset() {
        val timestamp = unixTime(2020, 9, 3)
        assertEquals(
            timestamp,
            getStartOfDayWithOffset(timestamp + LocalDate.HOUR_LENGTH),
        )
        LocalDate.setStartDayOffset(3, 30)
        assertEquals(
            (timestamp - LocalDate.DAY_LENGTH),
            getStartOfDayWithOffset(timestamp + 3 * LocalDate.HOUR_LENGTH + 29 * LocalDate.MINUTE_LENGTH),
        )
    }

    @Test
    fun testGetStartOfTodayWithOffset_priorToOffset() {
        val hourOffset = 3
        LocalDate.setStartDayOffset(hourOffset, 0)
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        val startOfYesterday = unixTime(2017, 1, 1, 0, 0)
        val priorToOffset = unixTime(2017, 1, 2, hourOffset - 1, 0)
        LocalDate.fixedLocalTime = priorToOffset
        val startOfTodayWithOffset = getStartOfTodayWithOffset()
        assertEquals(startOfYesterday, startOfTodayWithOffset)
    }

    @Test
    fun testGetStartOfTodayWithOffset_afterOffset() {
        val hourOffset = 3
        LocalDate.setStartDayOffset(hourOffset, 0)
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        val startOfToday = unixTime(2017, 1, 1, 0, 0)
        val afterOffset = unixTime(2017, 1, 1, hourOffset + 1, 0)
        LocalDate.fixedLocalTime = afterOffset
        val startOfTodayWithOffset = getStartOfTodayWithOffset()
        assertEquals(startOfToday, startOfTodayWithOffset)
    }

    private fun unixTime(year: Int, month: Int, day: Int): Long {
        return unixTime(year, month, day, 0, 0)
    }

    private fun unixTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        milliseconds: Long = 0
    ): Long {
        return LocalDateTime(
            year, month, day, hour, minute, (milliseconds / 1000).toInt(), 0
        ).toInstant(TimeZone.UTC).toEpochMilliseconds()
    }
}
