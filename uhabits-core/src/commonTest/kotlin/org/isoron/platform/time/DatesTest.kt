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

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.isoron.platform.core.BaseUnitTest
import org.isoron.platform.time.LocalDate.Companion.applyTimezone
import org.isoron.platform.time.LocalDate.Companion.getStartOfDay
import org.isoron.platform.time.LocalDate.Companion.getStartOfDayWithOffset
import org.isoron.platform.time.LocalDate.Companion.getStartOfToday
import org.isoron.platform.time.LocalDate.Companion.getStartOfTodayLocalDateTime
import org.isoron.platform.time.LocalDate.Companion.getStartOfTodayWithOffset
import org.isoron.platform.time.LocalDate.Companion.getUpcomingTimeInMillis
import org.isoron.platform.time.LocalDate.Companion.getWeekdaySequence
import org.isoron.platform.time.LocalDate.Companion.removeTimezone
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DatesTest : BaseUnitTest() {
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
        val utcTestTimeInMillis = unixTime(2015, Month.JANUARY, 11)
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
        val expectedStartOfDayUtc = unixTime(2017, Month.JANUARY, 1)
        val laterInTheDayUtc = unixTime(2017, Month.JANUARY, 1, 20, 0)
        val startOfDay = getStartOfDay(laterInTheDayUtc)
        assertEquals(expectedStartOfDayUtc, startOfDay)
    }

    @Test
    fun testGetStartOfToday() {
        val expectedStartOfDayUtc = unixTime(2017, Month.JANUARY, 1)
        val laterInTheDayUtc = unixTime(2017, Month.JANUARY, 1, 20, 0)
        LocalDate.fixedLocalTime = laterInTheDayUtc
        val startOfToday = getStartOfToday()
        assertEquals(expectedStartOfDayUtc, startOfToday)
    }

    @Test
    @Throws(Exception::class)
    fun testGetStartOfDayWithOffset() {
        val timestamp = unixTime(2020, Month.SEPTEMBER, 3)
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
        val startOfYesterday = unixTime(2017, Month.JANUARY, 1, 0, 0)
        val priorToOffset = unixTime(2017, Month.JANUARY, 2, hourOffset - 1, 0)
        LocalDate.fixedLocalTime = priorToOffset
        val startOfTodayWithOffset = getStartOfTodayWithOffset()
        assertEquals(startOfYesterday, startOfTodayWithOffset)
    }

    @Test
    fun testGetStartOfTodayWithOffset_afterOffset() {
        val hourOffset = 3
        LocalDate.setStartDayOffset(hourOffset, 0)
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        val startOfToday = unixTime(2017, Month.JANUARY, 1, 0, 0)
        val afterOffset = unixTime(2017, Month.JANUARY, 1, hourOffset + 1, 0)
        LocalDate.fixedLocalTime = afterOffset
        val startOfTodayWithOffset = getStartOfTodayWithOffset()
        assertEquals(startOfToday, startOfTodayWithOffset)
    }

    @Test
    fun testGetStartOfTodayLocalDateTime() {
        LocalDate.fixedLocalTime = FIXED_LOCAL_TIME
        val startOfDay = unixTime(2015, Month.JANUARY, 25, 0, 0)
        val expectedLocalDateTime = Instant.fromEpochMilliseconds(startOfDay).toLocalDateTime(
            TimeZone.UTC
        )
        assertEquals(expectedLocalDateTime, getStartOfTodayLocalDateTime())
    }

    @Test
    fun test_applyTimezone() {
        LocalDate.fixedTimeZone = TimeZone.of("Australia/Sydney")
        assertEquals(
            applyTimezone(unixTime(2017, Month.JULY, 30, 18, 0)),
            unixTime(2017, Month.JULY, 30, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 0, 0)),
            unixTime(2017, Month.SEPTEMBER, 29, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 10, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 11, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 12, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 2, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 13, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 3, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 22, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 12, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.SEPTEMBER, 30, 23, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 0, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 1, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 15, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 1, 59)),
            unixTime(2017, Month.SEPTEMBER, 30, 15, 59)
        )
        // DST begins
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 3, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 16, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 4, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 17, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 5, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 18, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 11, 0)),
            unixTime(2017, Month.OCTOBER, 1, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 12, 0)),
            unixTime(2017, Month.OCTOBER, 1, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 13, 0)),
            unixTime(2017, Month.OCTOBER, 1, 2, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 14, 0)),
            unixTime(2017, Month.OCTOBER, 1, 3, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 15, 0)),
            unixTime(2017, Month.OCTOBER, 1, 4, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 1, 19, 0)),
            unixTime(2017, Month.OCTOBER, 1, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.OCTOBER, 2, 19, 0)),
            unixTime(2017, Month.OCTOBER, 2, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Month.NOVEMBER, 30, 19, 0)),
            unixTime(2017, Month.NOVEMBER, 30, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.MARCH, 31, 0, 0)),
            unixTime(2018, Month.MARCH, 30, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.MARCH, 31, 12, 0)),
            unixTime(2018, Month.MARCH, 31, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.MARCH, 31, 18, 0)),
            unixTime(2018, Month.MARCH, 31, 7, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 0, 0)),
            unixTime(2018, Month.MARCH, 31, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 1, 0)),
            unixTime(2018, Month.MARCH, 31, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 1, 59)),
            unixTime(2018, Month.MARCH, 31, 14, 59)
        )
        // DST ends
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 2, 0)),
            unixTime(2018, Month.MARCH, 31, 16, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 3, 0)),
            unixTime(2018, Month.MARCH, 31, 17, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 4, 0)),
            unixTime(2018, Month.MARCH, 31, 18, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 10, 0)),
            unixTime(2018, Month.APRIL, 1, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Month.APRIL, 1, 18, 0)),
            unixTime(2018, Month.APRIL, 1, 8, 0)
        )
    }

    @Test
    fun test_removeTimezone() {
        LocalDate.fixedTimeZone = TimeZone.of("Australia/Sydney")
        assertEquals(
            removeTimezone(unixTime(2017, Month.JULY, 30, 8, 0)),
            unixTime(2017, Month.JULY, 30, 18, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 29, 14, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 0, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 10, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 1, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 11, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 2, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 3, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 13, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 12, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 22, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 13, 0)),
            unixTime(2017, Month.SEPTEMBER, 30, 23, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 14, 0)),
            unixTime(2017, Month.OCTOBER, 1, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 15, 0)),
            unixTime(2017, Month.OCTOBER, 1, 1, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 15, 59)),
            unixTime(2017, Month.OCTOBER, 1, 1, 59)
        )
        // DST begins
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 16, 0)),
            unixTime(2017, Month.OCTOBER, 1, 3, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 17, 0)),
            unixTime(2017, Month.OCTOBER, 1, 4, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.SEPTEMBER, 30, 18, 0)),
            unixTime(2017, Month.OCTOBER, 1, 5, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 0, 0)),
            unixTime(2017, Month.OCTOBER, 1, 11, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 1, 0)),
            unixTime(2017, Month.OCTOBER, 1, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 2, 0)),
            unixTime(2017, Month.OCTOBER, 1, 13, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 3, 0)),
            unixTime(2017, Month.OCTOBER, 1, 14, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 4, 0)),
            unixTime(2017, Month.OCTOBER, 1, 15, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 1, 8, 0)),
            unixTime(2017, Month.OCTOBER, 1, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.OCTOBER, 2, 8, 0)),
            unixTime(2017, Month.OCTOBER, 2, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Month.NOVEMBER, 30, 8, 0)),
            unixTime(2017, Month.NOVEMBER, 30, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 30, 13, 0)),
            unixTime(2018, Month.MARCH, 31, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 1, 0)),
            unixTime(2018, Month.MARCH, 31, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 7, 0)),
            unixTime(2018, Month.MARCH, 31, 18, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 13, 0)),
            unixTime(2018, Month.APRIL, 1, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 14, 0)),
            unixTime(2018, Month.APRIL, 1, 1, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 14, 59)),
            unixTime(2018, Month.APRIL, 1, 1, 59)
        )
        // DST ends
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 16, 0)),
            unixTime(2018, Month.APRIL, 1, 2, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 17, 0)),
            unixTime(2018, Month.APRIL, 1, 3, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.MARCH, 31, 18, 0)),
            unixTime(2018, Month.APRIL, 1, 4, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.APRIL, 1, 0, 0)),
            unixTime(2018, Month.APRIL, 1, 10, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Month.APRIL, 1, 8, 0)),
            unixTime(2018, Month.APRIL, 1, 18, 0)
        )
    }

    @Test
    fun testGetUpcomingTimeInMillis() {
        LocalDate.fixedLocalTime = FIXED_LOCAL_TIME
        LocalDate.fixedTimeZone = TimeZone.UTC
        val expected = unixTime(2015, Month.JANUARY, 25, 10, 1)
        val upcomingTimeMillis = getUpcomingTimeInMillis(10, 1)
        assertEquals(expected, upcomingTimeMillis)
    }

    @Test
    fun testDaysSince2000() {
        val zeroDays =  daysSince2000(2000, Month.JANUARY, 1)
        assertEquals(0, zeroDays)

        val oneYearWithLeapYear =  daysSince2000(2001, Month.JANUARY, 1)
        assertEquals(366, oneYearWithLeapYear)

        val fourYearsWithLeapYear =  daysSince2000(2004, Month.JANUARY, 1)
        assertEquals(1461, fourYearsWithLeapYear)

        val oneYearPrior =  daysSince2000(1999, Month.JANUARY, 1)
        assertEquals(-365, oneYearPrior)
    }

    private fun unixTime(year: Int, month: Month, day: Int): Long {
        return unixTime(year, month, day, 0, 0)
    }

    private fun unixTime(
        year: Int,
        month: Month,
        day: Int,
        hour: Int,
        minute: Int,
        milliseconds: Long = 0
    ): Long {
        return LocalDateTime(
            year, month, day, hour, minute, (milliseconds / 1000).toInt(), 0
        ).toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    companion object {
        const val JAN_1_2000_IN_UNIX_TIME = 946684800000L
    }
}
