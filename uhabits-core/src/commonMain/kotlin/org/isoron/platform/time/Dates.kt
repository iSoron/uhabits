/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import kotlin.math.ceil

private var currentToday: LocalDate? = null
private var currentFirstWeekday: DayOfWeek? = null

fun getToday(): LocalDate =
    currentToday ?: error("getToday() called before setToday()")

fun setToday(date: LocalDate) {
    currentToday = date
}

fun resetToday() {
    currentToday = null
}

fun getFirstWeekday(): DayOfWeek =
    currentFirstWeekday ?: DayOfWeek.entries[getFirstWeekdayNumberAccordingToLocale() - 1]

fun setFirstWeekdayNumber(value: Int?) {
    currentFirstWeekday = value?.let { DayOfWeek.entries[it - 1] }
}

enum class DayOfWeek(val daysSinceSunday: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6)
}

data class LocalDate(val daysSince2000: Int) : Comparable<LocalDate> {
    override fun compareTo(other: LocalDate): Int =
        daysSince2000.compareTo(other.daysSince2000)

    var yearCache = -1
    var monthCache = -1
    var dayCache = -1

    constructor(year: Int, month: Int, day: Int) :
        this(daysSince2000(year, month, day))

    val dayOfWeek: DayOfWeek
        get() {
            val rem = daysSince2000 % 7
            val mod = if (rem < 0) rem + 7 else rem
            return when (mod) {
                0 -> DayOfWeek.SATURDAY
                1 -> DayOfWeek.SUNDAY
                2 -> DayOfWeek.MONDAY
                3 -> DayOfWeek.TUESDAY
                4 -> DayOfWeek.WEDNESDAY
                5 -> DayOfWeek.THURSDAY
                else -> DayOfWeek.FRIDAY
            }
        }

    val year: Int
        get() {
            if (yearCache < 0) updateYearMonthDayCache()
            return yearCache
        }

    val month: Int
        get() {
            if (monthCache < 0) updateYearMonthDayCache()
            return monthCache
        }

    val day: Int
        get() {
            if (dayCache < 0) updateYearMonthDayCache()
            return dayCache
        }

    val monthLength: Int
        get() = when (month) {
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }

    val yearLength: Int
        get() = if (isLeapYear(year)) 366 else 365

    private fun updateYearMonthDayCache() {
        var currYear = 2000
        var currDay = 0
        if (daysSince2000 < 0) {
            currYear -= 400
            currDay -= 146097
        }
        while (true) {
            val currYearLength = if (isLeapYear(currYear)) 366 else 365
            if (daysSince2000 < currDay + currYearLength) {
                yearCache = currYear
                break
            } else {
                currYear++
                currDay += currYearLength
            }
        }
        var currMonth = 1
        val monthOffset = if (isLeapYear(currYear)) leapOffset else nonLeapOffset
        while (true) {
            if (daysSince2000 < currDay + monthOffset[currMonth]) {
                monthCache = currMonth
                break
            } else {
                currMonth++
            }
        }
        currDay += monthOffset[currMonth - 1]
        dayCache = daysSince2000 - currDay + 1
    }

    fun isOlderThan(other: LocalDate): Boolean {
        return daysSince2000 < other.daysSince2000
    }

    fun isNewerThan(other: LocalDate): Boolean {
        return daysSince2000 > other.daysSince2000
    }

    fun plus(days: Int): LocalDate {
        return LocalDate(daysSince2000 + days)
    }

    fun minus(days: Int): LocalDate {
        return LocalDate(daysSince2000 - days)
    }

    fun daysUntil(other: LocalDate): Int {
        return other.daysSince2000 - this.daysSince2000
    }

    val unixTime: Long
        get() = EPOCH_2000_MILLIS + daysSince2000.toLong() * MILLIS_PER_DAY

    fun startOfWeek(firstWeekday: DayOfWeek): LocalDate {
        var delta = dayOfWeek.daysSinceSunday - firstWeekday.daysSinceSunday
        if (delta < 0) delta += 7
        return minus(delta)
    }

    fun startOfMonth(): LocalDate = LocalDate(year, month, 1)

    fun startOfQuarter(): LocalDate {
        val quarterMonth = ((month - 1) / 3) * 3 + 1
        return LocalDate(year, quarterMonth, 1)
    }

    fun startOfYear(): LocalDate = LocalDate(year, 1, 1)

    fun toCSVString(): String {
        val y = year.toString().padStart(4, '0')
        val m = month.toString().padStart(2, '0')
        val d = day.toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    override fun toString(): String {
        return "LocalDate($year-$month-$day)"
    }

    companion object {
        private const val EPOCH_2000_MILLIS = 946684800000L
        private const val MILLIS_PER_DAY = 86400000L

        fun fromUnixTime(millis: Long): LocalDate {
            val diff = millis - EPOCH_2000_MILLIS
            val days = if (diff >= 0) diff / MILLIS_PER_DAY else (diff - MILLIS_PER_DAY + 1) / MILLIS_PER_DAY
            return LocalDate(days.toInt())
        }
    }
}

interface LocalDateFormatter {
    fun shortWeekdayName(weekday: DayOfWeek): String
    fun shortWeekdayName(date: LocalDate): String
    fun shortMonthName(date: LocalDate): String
    fun longWeekdayName(weekday: DayOfWeek): String
    fun longMonthName(date: LocalDate): String
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

val leapOffset = arrayOf(
    0, 31, 60, 91, 121, 152, 182,
    213, 244, 274, 305, 335, 366
)
val nonLeapOffset = arrayOf(
    0, 31, 59, 90, 120, 151, 181,
    212, 243, 273, 304, 334, 365
)

private fun daysSince2000(year: Int, month: Int, day: Int): Int {
    var result = 365 * (year - 2000)
    result += ceil((year - 2000) / 4.0).toInt()
    result -= ceil((year - 2000) / 100.0).toInt()
    result += ceil((year - 2000) / 400.0).toInt()
    result += if (isLeapYear(year)) {
        leapOffset[month - 1]
    } else {
        nonLeapOffset[month - 1]
    }
    result += (day - 1)
    return result
}

enum class TruncateField {
    DAY, WEEK_NUMBER, MONTH, QUARTER, YEAR
}

fun getWeekdaySequence(firstWeekday: DayOfWeek): List<DayOfWeek> {
    val allDays = DayOfWeek.entries
    return (0 until 7).map { offset ->
        allDays[(firstWeekday.daysSinceSunday + offset) % 7]
    }
}

expect fun getFirstWeekdayNumberAccordingToLocale(): Int

expect fun computeToday(hourOffset: Int = 0, minuteOffset: Int = 0): LocalDate

fun countWeekdayOccurrencesInMonth(startOfMonth: LocalDate): Array<Int> {
    val weekday = (startOfMonth.dayOfWeek.daysSinceSunday + 1) % 7
    val freq = Array(7) { 0 }
    for (day in weekday until weekday + startOfMonth.monthLength) {
        freq[day % 7] += 1
    }
    return freq
}
