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

import kotlin.math.*

enum class DayOfWeek(val index: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
}

data class Timestamp(val unixTimeInMillis: Long)

data class LocalDate(val year: Int,
                     val month: Int,
                     val day: Int) {

    fun isOlderThan(other: LocalDate): Boolean {
        if (other.year != year) return other.year > year
        if (other.month != month) return other.month > month
        return other.day > day
    }

    fun isNewerThan(other: LocalDate): Boolean {
        if (this == other) return false
        return other.isOlderThan(this)
    }

    init {
        if ((month <= 0) or (month >= 13)) throw(IllegalArgumentException())
        if ((day <= 0) or (day >= 32)) throw(IllegalArgumentException())
    }
}

interface LocalDateCalculator {
    fun plusDays(date: LocalDate, days: Int): LocalDate
    fun dayOfWeek(date: LocalDate): DayOfWeek
    fun toTimestamp(date: LocalDate): Timestamp
    fun fromTimestamp(timestamp: Timestamp): LocalDate
}

fun LocalDateCalculator.distanceInDays(d1: LocalDate, d2: LocalDate): Int {
    val t1 = toTimestamp(d1)
    val t2 = toTimestamp(d2)
    val dayLength = 24 * 60 * 60 * 1000
    return abs((t2.unixTimeInMillis - t1.unixTimeInMillis) / dayLength).toInt()
}

fun LocalDateCalculator.minusDays(date: LocalDate, days: Int): LocalDate {
    return plusDays(date, -days)
}

interface LocalDateFormatter {
    fun shortWeekdayName(date: LocalDate): String
    fun shortMonthName(date: LocalDate): String
}