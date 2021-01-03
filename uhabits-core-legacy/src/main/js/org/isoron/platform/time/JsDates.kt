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

import kotlin.js.*

fun LocalDate.toJsDate(): Date {
    return Date(year, month - 1, day)
}

class JsDateFormatter(private val locale: String) : LocalDateFormatter {
    override fun shortWeekdayName(date: LocalDate): String {
        val options = dateLocaleOptions { weekday = "short" }
        return date.toJsDate().toLocaleString(locale, options)
    }

    override fun shortMonthName(date: LocalDate): String {
        val options = dateLocaleOptions { month = "short" }
        return date.toJsDate().toLocaleString(locale, options)
    }
}