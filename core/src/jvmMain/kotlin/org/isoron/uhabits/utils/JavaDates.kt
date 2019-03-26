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

import java.util.*
import java.util.Calendar.*

class JavaLocalDateFormatter(private val locale: Locale) : LocalDateFormatter {
    override fun shortWeekdayName(date: LocalDate): String {
        val d = GregorianCalendar(date.year, date.month - 1, date.day)
        return d.getDisplayName(DAY_OF_WEEK, SHORT, locale);
    }
}

class JavaLocalDateCalculator : LocalDateCalculator {
    override fun plusDays(date: LocalDate, days: Int): LocalDate {
        val d = GregorianCalendar(date.year, date.month - 1, date.day)
        d.add(Calendar.DAY_OF_MONTH, days)
        return LocalDate(d.get(Calendar.YEAR),
                         d.get(Calendar.MONTH) + 1,
                         d.get(Calendar.DAY_OF_MONTH))
    }
}