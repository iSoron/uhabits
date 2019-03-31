/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.platform

import junit.framework.TestCase.*
import org.isoron.platform.time.*
import org.junit.*
import java.util.*
import java.util.Calendar.*


class JavaDatesTest {
    private val d1 = LocalDate(2019, 3, 25)
    private val d2 = LocalDate(2019, 4, 4)
    private val d3 = LocalDate(2019, 5, 12)

    @Test
    fun plusMinusDays() {
        val today = LocalDate(2019, 3, 25)
        assertEquals(today.minus(28), LocalDate(2019, 2, 25))
        assertEquals(today.plus(7), LocalDate(2019, 4, 1))
        assertEquals(today.plus(42), LocalDate(2019, 5, 6))
    }

    @Test
    fun shortMonthName() {
        var fmt = JavaLocalDateFormatter(Locale.US)
        assertEquals("Mon", fmt.shortWeekdayName(d1))
        assertEquals("Thu", fmt.shortWeekdayName(d2))
        assertEquals("Sun", fmt.shortWeekdayName(d3))
        assertEquals("Mar", fmt.shortMonthName(d1))
        assertEquals("Apr", fmt.shortMonthName(d2))
        assertEquals("May", fmt.shortMonthName(d3))

        fmt = JavaLocalDateFormatter(Locale.JAPAN)
        assertEquals("月", fmt.shortWeekdayName(d1))
        assertEquals("木", fmt.shortWeekdayName(d2))
        assertEquals("日", fmt.shortWeekdayName(d3))
        assertEquals("3月", fmt.shortMonthName(d1))
        assertEquals("4月", fmt.shortMonthName(d2))
        assertEquals("5月", fmt.shortMonthName(d3))
    }

    @Test
    fun weekDay() {
        assertEquals(DayOfWeek.SUNDAY, LocalDate(2015, 1, 25).dayOfWeek)
        assertEquals(DayOfWeek.MONDAY, LocalDate(2017, 7, 3).dayOfWeek)
    }

    @Test
    fun timestamps() {
        val timestamps = listOf(Timestamp(1555977600000),
                                Timestamp(968716800000),
                                Timestamp(946684800000))
        val dates = listOf(LocalDate(2019, 4, 23),
                           LocalDate(2000, 9, 12),
                           LocalDate(2000, 1, 1))
        assertEquals(timestamps, dates.map { d -> d.timestamp })
        assertEquals(dates, timestamps.map { t -> t.localDate })
    }

    @Test
    fun isOlderThan() {
        val ref = LocalDate(2010, 10, 5)
        assertTrue(ref.isOlderThan(LocalDate(2010, 10, 10)))
        assertTrue(ref.isOlderThan(LocalDate(2010, 11, 4)))
        assertTrue(ref.isOlderThan(LocalDate(2011, 1, 5)))
        assertTrue(ref.isOlderThan(LocalDate(2015, 3, 1)))

        assertFalse(ref.isOlderThan(LocalDate(2010, 10, 5)))
        assertFalse(ref.isOlderThan(LocalDate(2010, 10, 4)))
        assertFalse(ref.isOlderThan(LocalDate(2010, 9, 1)))
        assertFalse(ref.isOlderThan(LocalDate(2005, 10, 5)))
    }

    @Test
    fun testDistanceInDays() {
        val d1 = LocalDate(2019, 5, 10)
        val d2 = LocalDate(2019, 5, 30)
        val d3 = LocalDate(2019, 6, 5)

        assertEquals(0, d1.distanceTo(d1))
        assertEquals(20, d1.distanceTo(d2))
        assertEquals(20, d2.distanceTo(d1))
        assertEquals(26, d1.distanceTo(d3))
        assertEquals(6, d2.distanceTo(d3))
    }

    @Test
    fun gregorianCalendarConversion() {
        fun check(cal: GregorianCalendar, daysSince2000: Int) {
            val year = cal.get(YEAR)
            val month = cal.get(MONTH) + 1
            val day = cal.get(DAY_OF_MONTH)
            val weekday = cal.get(DAY_OF_WEEK)
            val date = LocalDate(year, month, day)
            val millisSince1970 = cal.timeInMillis
            val msg = "date=$year-$month-$day offset=$daysSince2000"

            assertEquals(msg, daysSince2000, date.daysSince2000)
            assertEquals(msg, year, date.year)
            assertEquals(msg, month, date.month)
            assertEquals(msg, day, date.day)
            assertEquals(msg, weekday, date.dayOfWeek.index + 1)
            assertEquals(msg, millisSince1970, date.timestamp.millisSince1970)
            assertEquals(msg, date, date.timestamp.localDate)
        }

        val cal = GregorianCalendar()
        cal.timeZone = TimeZone.getTimeZone("GMT")
        cal.set(MILLISECOND, 0)
        cal.set(SECOND, 0)
        cal.set(MINUTE, 0)
        cal.set(HOUR_OF_DAY, 0)
        cal.set(DAY_OF_MONTH, 1)
        cal.set(MONTH, 0)
        cal.set(YEAR, 2000)

        // Check all dates from year 2000 until 2400
        for(offset in 0..146097) {
            check(cal, offset)
            cal.add(DAY_OF_YEAR, 1)
        }
    }
}
