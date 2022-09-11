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

import java.text.DateFormat
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.LONG
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.SHORT
import java.util.Calendar.YEAR
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

fun LocalDate.toGregorianCalendar(): GregorianCalendar {
    val cal = GregorianCalendar()
    cal.timeZone = TimeZone.getTimeZone("GMT")
    cal.set(MILLISECOND, 0)
    cal.set(SECOND, 0)
    cal.set(MINUTE, 0)
    cal.set(HOUR_OF_DAY, 0)
    cal.set(YEAR, this.year)
    cal.set(MONTH, this.month - 1)
    cal.set(DAY_OF_MONTH, this.day)
    return cal
}

class JavaLocalDateFormatter(private val locale: Locale) : LocalDateFormatter {
    override fun shortMonthName(date: LocalDate): String {
        val cal = date.toGregorianCalendar()
        val longName = cal.getDisplayName(MONTH, LONG, locale)
        val shortName = cal.getDisplayName(MONTH, SHORT, locale)

        // For some locales, such as Japan, SHORT name is exceedingly short
        return if (longName.length <= 3) longName else shortName
    }

    override fun shortWeekdayName(weekday: DayOfWeek): String {
        val cal = GregorianCalendar()
        cal.set(DAY_OF_WEEK, weekday.daysSinceSunday - 1)
        return shortWeekdayName(LocalDate(cal.get(YEAR), cal.get(MONTH) + 1, cal.get(DAY_OF_MONTH)))
    }

    override fun shortWeekdayName(date: LocalDate): String {
        val cal = date.toGregorianCalendar()
        return cal.getDisplayName(DAY_OF_WEEK, SHORT, locale)
    }

    fun longFormat(date: LocalDate): String {
        val df = DateFormat.getDateInstance(DateFormat.LONG, locale)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date.toGregorianCalendar().time)
    }
}
