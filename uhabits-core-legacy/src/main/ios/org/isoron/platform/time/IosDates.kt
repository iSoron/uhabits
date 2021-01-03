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

import platform.Foundation.*

fun LocalDate.toNsDate(): NSDate {
    val calendar = NSCalendar.calendarWithIdentifier(NSCalendarIdentifierGregorian)!!
    val dc = NSDateComponents()
    dc.year = year.toLong()
    dc.month = month.toLong()
    dc.day = day.toLong()
    dc.hour = 13
    dc.minute = 0
    return calendar.dateFromComponents(dc)!!
}

class IosLocalDateFormatter(val locale: String) : LocalDateFormatter {

    constructor() : this(NSLocale.preferredLanguages[0] as String)

    private val fmt = NSDateFormatter()

    init {
        fmt.setLocale(NSLocale.localeWithLocaleIdentifier(locale))
    }

    override fun shortWeekdayName(date: LocalDate): String {
        fmt.dateFormat = "EEE"
        return fmt.stringFromDate(date.toNsDate())
    }

    override fun shortMonthName(date: LocalDate): String {
        fmt.dateFormat = "MMM"
        return fmt.stringFromDate(date.toNsDate())
    }

}