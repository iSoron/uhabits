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

import io.fluidsonic.locale.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import org.isoron.platform.i18n.getDefault
import kotlin.math.abs
import kotlin.math.ceil

enum class DayOfWeek(val daysSinceSunday: Int) {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
}

data class LocalDate(val daysSince2000: Int) {

    var yearCache = -1
    var monthCache = -1
    var dayCache = -1

    constructor(year: Int, month: Int, day: Int) :
        this(daysSince2000(year, month, day))

    val dayOfWeek: DayOfWeek
        get() {
            return when (daysSince2000 % 7) {
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

    fun distanceTo(other: LocalDate): Int {
        return abs(daysSince2000 - other.daysSince2000)
    }

    override fun toString(): String {
        return "LocalDate($year-$month-$day)"
    }

    companion object {
        /**
         * Number of milliseconds in one second.
         */
        const val SECOND_LENGTH: Long = 1000

        /**
         * Number of milliseconds in one minute.
         */
        const val MINUTE_LENGTH: Long = 60 * SECOND_LENGTH

        /**
         * Number of milliseconds in one hour.
         */
        const val HOUR_LENGTH: Long = 60 * MINUTE_LENGTH

        /**
         * Number of milliseconds in one day.
         */
        const val DAY_LENGTH: Long = 24 * HOUR_LENGTH

        var fixedLocalTime: Long? = null
        var fixedTimeZone: TimeZone? = null
        var fixedLocale: Locale? = null
        var startDayHourOffset: Int = 0
        var startDayMinuteOffset: Int = 0

        fun setStartDayOffset(hourOffset: Int, minuteOffset: Int) {
            startDayHourOffset = hourOffset
            startDayMinuteOffset = minuteOffset
        }

        fun getLocalTime(testTimeInMillis: Long? = null): Long {
            if (fixedLocalTime != null) return fixedLocalTime as Long

            val tz = getTimeZone()
            val now = testTimeInMillis ?: Clock.System.now().toEpochMilliseconds()
            return now + (tz.offsetAt(Instant.fromEpochMilliseconds(now)).totalSeconds * 1000)
        }

        fun getTimeZone(): TimeZone {
            return fixedTimeZone ?: TimeZone.currentSystemDefault()
        }

        fun getLocale(): Locale {
            return fixedLocale ?: Locale.getDefault()
        }

        /**
         * Returns a vector of exactly seven integers, where the first integer is
         * the provided firstWeekday number, and each subsequent number is the
         * previous number plus 1, wrapping back to 1 after 7. For example,
         * providing 3 as firstWeekday returns {3,4,5,6,7,1,2}
         *
         * This function is supposed to be used to construct a sequence of weekday
         * number following java.util.Calendar conventions.
         */
        fun getWeekdaySequence(firstWeekday: Int): Array<Int> {
            return arrayOf(
                (firstWeekday - 1) % 7 + 1,
                (firstWeekday) % 7 + 1,
                (firstWeekday + 1) % 7 + 1,
                (firstWeekday + 2) % 7 + 1,
                (firstWeekday + 3) % 7 + 1,
                (firstWeekday + 4) % 7 + 1,
                (firstWeekday + 5) % 7 + 1,
            )
        }

        fun getStartOfDay(timestamp: Long): Long = (timestamp / DAY_LENGTH) * DAY_LENGTH

        fun getStartOfToday(): Long = getStartOfDay(getLocalTime())

        fun getStartOfDayWithOffset(timestamp: Long): Long {
            val offset = startDayHourOffset * HOUR_LENGTH + startDayMinuteOffset * MINUTE_LENGTH
            return getStartOfDay(timestamp - offset)
        }

        fun getStartOfTodayWithOffset(): Long = getStartOfDayWithOffset(getLocalTime())
    }
}

interface LocalDateFormatter {
    fun shortWeekdayName(weekday: DayOfWeek): String
    fun shortWeekdayName(date: LocalDate): String
    fun shortMonthName(date: LocalDate): String
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
