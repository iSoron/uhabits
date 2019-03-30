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


class JavaDatesTest {
    private val calc = JavaLocalDateCalculator()
    private val d1 = LocalDate(2019, 3, 25)
    private val d2 = LocalDate(2019, 4, 4)
    private val d3 = LocalDate(2019, 5, 12)

    @Test
    fun plusMinusDays() {
        val today = LocalDate(2019, 3, 25)
        assertEquals(calc.minusDays(today, 28), LocalDate(2019, 2, 25))
        assertEquals(calc.plusDays(today, 7), LocalDate(2019, 4, 1))
        assertEquals(calc.plusDays(today, 42), LocalDate(2019, 5, 6))
    }

    @Test
    fun shortMonthName() {
        var fmt = JavaLocalDateFormatter(Locale.US)
        assertEquals(fmt.shortWeekdayName(d1), "Mon")
        assertEquals(fmt.shortWeekdayName(d2), "Thu")
        assertEquals(fmt.shortWeekdayName(d3), "Sun")
        assertEquals(fmt.shortMonthName(d1), "Mar")
        assertEquals(fmt.shortMonthName(d2), "Apr")
        assertEquals(fmt.shortMonthName(d3), "May")

        fmt = JavaLocalDateFormatter(Locale.JAPAN)
        assertEquals(fmt.shortWeekdayName(d1), "月")
        assertEquals(fmt.shortWeekdayName(d2), "木")
        assertEquals(fmt.shortWeekdayName(d3), "日")
        assertEquals(fmt.shortMonthName(d1), "3月")
        assertEquals(fmt.shortMonthName(d2), "4月")
        assertEquals(fmt.shortMonthName(d3), "5月")
    }

    @Test
    fun weekDay() {
        assertEquals(DayOfWeek.SUNDAY, calc.dayOfWeek(LocalDate(2015, 1, 25)))
        assertEquals(DayOfWeek.MONDAY, calc.dayOfWeek(LocalDate(2017, 7, 3)))
    }

    @Test
    fun timestamps() {
        val timestamps = listOf(Timestamp(1555977600000),
                                Timestamp(968716800000),
                                Timestamp(0))
        val dates = listOf(LocalDate(2019, 4, 23),
                           LocalDate(2000, 9, 12),
                           LocalDate(1970, 1, 1))
        assertEquals(timestamps, dates.map { d -> calc.toTimestamp(d) })
        assertEquals(dates, timestamps.map { t -> calc.fromTimestamp(t) })
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

        assertEquals(0, calc.distanceInDays(d1, d1))
        assertEquals(20, calc.distanceInDays(d1, d2))
        assertEquals(20, calc.distanceInDays(d2, d1))
        assertEquals(26, calc.distanceInDays(d1, d3))
        assertEquals(6, calc.distanceInDays(d2, d3))
    }
}
