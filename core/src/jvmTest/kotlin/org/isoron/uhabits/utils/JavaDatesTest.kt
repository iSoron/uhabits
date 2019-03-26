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

package org.isoron.uhabits.utils

import junit.framework.TestCase.*
import org.junit.*
import java.util.*


class JavaDatesTest {
    val calc = JavaLocalDateCalculator()

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
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 3, 25)), "Mon")
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 4, 4)), "Thu")
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 5, 12)), "Sun")

        fmt = JavaLocalDateFormatter(Locale.JAPAN)
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 3, 25)), "月")
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 4, 4)), "木")
        assertEquals(fmt.shortWeekdayName(LocalDate(2019, 5, 12)), "日")
    }
}