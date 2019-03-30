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

import java.lang.Math.*
import java.util.*
import java.util.Calendar.*

fun LocalDate.toGregorianCalendar(): GregorianCalendar {
    val cal = GregorianCalendar(TimeZone.getTimeZone("GMT"))
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.YEAR, this.year)
    cal.set(Calendar.MONTH, this.month - 1)
    cal.set(Calendar.DAY_OF_MONTH, this.day)
    return cal
}

fun GregorianCalendar.toLocalDate(): LocalDate {
    return LocalDate(this.get(YEAR),
                     this.get(MONTH) + 1,
                     this.get(DAY_OF_MONTH))
}

class JavaLocalDateFormatter(private val locale: Locale) : LocalDateFormatter {
    override fun shortMonthName(date: LocalDate): String {
        val cal = date.toGregorianCalendar()
        val longName = cal.getDisplayName(MONTH, LONG, locale)
        val shortName = cal.getDisplayName(MONTH, SHORT, locale)

        // For some locales, such as Japan, SHORT name is exceedingly short
        return if (longName.length <= 3) longName else shortName
    }

    override fun shortWeekdayName(date: LocalDate): String {
        val cal = date.toGregorianCalendar()
        return cal.getDisplayName(DAY_OF_WEEK, SHORT, locale);
    }
}

class JavaLocalDateCalculator : LocalDateCalculator {
    override fun toTimestamp(date: LocalDate): Timestamp {
        val cal = date.toGregorianCalendar()
        return Timestamp(cal.timeInMillis)
    }

    override fun fromTimestamp(timestamp: Timestamp): LocalDate {
        val cal = GregorianCalendar(TimeZone.getTimeZone("GMT"))
        cal.timeInMillis = timestamp.unixTimeInMillis
        return cal.toLocalDate()
    }

    override fun dayOfWeek(date: LocalDate): DayOfWeek {
        val cal = date.toGregorianCalendar()
        return when (cal.get(DAY_OF_WEEK)) {
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            else -> DayOfWeek.FRIDAY
        }
    }

    override fun plusDays(date: LocalDate, days: Int): LocalDate {
        val cal = date.toGregorianCalendar()
        cal.add(Calendar.DAY_OF_MONTH, days)
        return cal.toLocalDate()
    }
}