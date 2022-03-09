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
package org.isoron.uhabits.core.models

import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.utils.DateFormats.Companion.getCSVDateFormat
import org.isoron.uhabits.core.utils.DateFormats.Companion.getDialogDateFormat
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfTodayCalendar
import org.isoron.uhabits.core.utils.DateUtils.Companion.truncate
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

data class Timestamp(var unixTime: Long) : Comparable<Timestamp> {

    constructor(cal: GregorianCalendar) : this(cal.timeInMillis)

    fun toLocalDate(): LocalDate {
        val millisSince2000 = unixTime - 946684800000L
        val daysSince2000 = (millisSince2000 / 86400000).toInt()
        return LocalDate(daysSince2000)
    }

    /**
     * Returns -1 if this timestamp is older than the given timestamp, 1 if this
     * timestamp is newer, or zero if they are equal.
     */
    override fun compareTo(other: Timestamp): Int {
        return java.lang.Long.signum(unixTime - other.unixTime)
    }

    operator fun minus(days: Int): Timestamp {
        return plus(-days)
    }

    operator fun plus(days: Int): Timestamp {
        return Timestamp(unixTime + DAY_LENGTH * days)
    }

    /**
     * Returns the number of days between this timestamp and the given one. If
     * the other timestamp equals this one, returns zero. If the other timestamp
     * is older than this one, returns a negative number.
     */
    fun daysUntil(other: Timestamp): Int {
        return ((other.unixTime - unixTime) / DAY_LENGTH).toInt()
    }

    fun isNewerThan(other: Timestamp): Boolean {
        return compareTo(other) > 0
    }

    fun isOlderThan(other: Timestamp): Boolean {
        return compareTo(other) < 0
    }

    fun toJavaDate(): Date {
        return Date(unixTime)
    }

    fun toCalendar(): GregorianCalendar {
        val day = GregorianCalendar(TimeZone.getTimeZone("GMT"))
        day.timeInMillis = unixTime
        return day
    }

    fun toDialogDateString(): String {
        return getDialogDateFormat().format(Date(unixTime))
    }

    override fun toString(): String {
        return getCSVDateFormat().format(Date(unixTime))
    }

    /**
     * Returns an integer corresponding to the day of the week. Saturday maps
     * to 0, Sunday maps to 1, and so on.
     */
    val weekday: Int
        get() = toCalendar()[Calendar.DAY_OF_WEEK] % 7

    fun truncate(field: DateUtils.TruncateField?, firstWeekday: Int): Timestamp {
        return Timestamp(
            truncate(
                field!!,
                unixTime,
                firstWeekday
            )
        )
    }

    companion object {
        const val DAY_LENGTH: Long = 86400000
        val ZERO = Timestamp(0)
        fun fromLocalDate(date: LocalDate): Timestamp {
            return Timestamp(946684800000L + date.daysSince2000 * 86400000L)
        }

        fun from(year: Int, javaMonth: Int, day: Int): Timestamp {
            val cal = getStartOfTodayCalendar()
            cal[year, javaMonth, day, 0, 0] = 0
            return Timestamp(cal.timeInMillis)
        }

        /**
         * Given two timestamps, returns whichever timestamp is the oldest one.
         */
        fun oldest(first: Timestamp, second: Timestamp): Timestamp {
            return if (first.unixTime < second.unixTime) first else second
        }
    }

    init {
        require(unixTime >= 0) { "Invalid unix time: $unixTime" }
        if (unixTime % DAY_LENGTH != 0L) unixTime = unixTime / DAY_LENGTH * DAY_LENGTH
    }
}
