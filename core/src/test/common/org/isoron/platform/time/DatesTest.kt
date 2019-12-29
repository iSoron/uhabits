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

package org.isoron.platform.time

import org.isoron.*
import kotlin.test.*

class DatesTest {
    private val d1 = LocalDate(2019, 3, 25)
    private val d2 = LocalDate(2019, 4, 4)
    private val d3 = LocalDate(2019, 5, 12)

    @Test
    fun testPlusMinusDays() {
        val today = LocalDate(2019, 3, 25)
        assertEquals(today.minus(28), LocalDate(2019, 2, 25))
        assertEquals(today.plus(7), LocalDate(2019, 4, 1))
        assertEquals(today.plus(42), LocalDate(2019, 5, 6))
    }

    @Test
    fun testFormatter() {
        var fmt = DependencyResolver.getDateFormatter(Locale.US)
        assertEquals("Mon", fmt.shortWeekdayName(d1))
        assertEquals("Thu", fmt.shortWeekdayName(d2))
        assertEquals("Sun", fmt.shortWeekdayName(d3))
        assertEquals("Mar", fmt.shortMonthName(d1))
        assertEquals("Apr", fmt.shortMonthName(d2))
        assertEquals("May", fmt.shortMonthName(d3))

        fmt = DependencyResolver.getDateFormatter(Locale.JAPAN)
        assertEquals("月", fmt.shortWeekdayName(d1))
        assertEquals("木", fmt.shortWeekdayName(d2))
        assertEquals("日", fmt.shortWeekdayName(d3))
        assertEquals("3月", fmt.shortMonthName(d1))
        assertEquals("4月", fmt.shortMonthName(d2))
        assertEquals("5月", fmt.shortMonthName(d3))
    }

    @Test
    fun testTimestamps() {
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
    fun testIsOlderThan() {
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
    fun testGregorianCalendarConversion() {
        fun check(daysSince2000: Int,
                  expectedYear: Int,
                  expectedMonth: Int,
                  expectedDay: Int,
                  expectedWeekday: Int) {
            val date = LocalDate(daysSince2000)
            assertEquals(expectedYear, date.year)
            assertEquals(expectedMonth, date.month)
            assertEquals(expectedDay, date.day)
            assertEquals(expectedWeekday, date.dayOfWeek.index)
            assertEquals(date, date.timestamp.localDate)
        }

        check(0, 2000, 1, 1, 6)
        check(626, 2001, 9, 18, 2)
        check(915, 2002, 7, 4, 4)
        check(2759, 2007, 7, 22, 0)
        check(2791, 2007, 8, 23, 4)
        check(6524, 2017, 11, 11, 6)
        check(7517, 2020, 7, 31, 5)
        check(10031, 2027, 6, 19, 6)
        check(13091, 2035, 11, 4, 0)
        check(14849, 2040, 8, 27, 1)
        check(17330, 2047, 6, 13, 4)
        check(20566, 2056, 4, 22, 6)
        check(23617, 2064, 8, 29, 5)
        check(27743, 2075, 12, 16, 1)
        check(31742, 2086, 11, 27, 3)
        check(36659, 2100, 5, 15, 6)
        check(39224, 2107, 5, 24, 2)
        check(39896, 2109, 3, 26, 2)
        check(40819, 2111, 10, 5, 1)
        check(43983, 2120, 6, 3, 1)
        check(46893, 2128, 5, 22, 6)
        check(51013, 2139, 9, 2, 3)
        check(55542, 2152, 1, 26, 3)
        check(58817, 2161, 1, 13, 2)
        check(63769, 2174, 8, 5, 5)
        check(64893, 2177, 9, 2, 2)
        check(66840, 2183, 1, 1, 3)
        check(68011, 2186, 3, 17, 5)
        check(70060, 2191, 10, 26, 3)
        check(70733, 2193, 8, 29, 4)
    }
}
