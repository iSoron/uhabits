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
package org.isoron.uhabits.core.utils

import org.isoron.uhabits.core.models.Timestamp
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.SHORT
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

abstract class DateUtils {
    companion object {
        private var fixedLocalTime: Long? = null
        private var fixedTimeZone: TimeZone? = null
        private var fixedLocale: Locale? = null
        private var startDayHourOffset: Int = 0
        private var startDayMinuteOffset: Int = 0

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

        @JvmStatic
        fun applyTimezone(localTimestamp: Long): Long {
            val tz: TimeZone = getTimeZone()
            return localTimestamp - tz.getOffset(localTimestamp - tz.getOffset(localTimestamp))
        }

        @JvmStatic
        fun formatHeaderDate(day: GregorianCalendar): String {
            val locale = getLocale()
            val dayOfMonth: String = day.get(DAY_OF_MONTH).toString()
            val dayOfWeek = day.getDisplayName(DAY_OF_WEEK, SHORT, locale)
            return dayOfWeek + "\n" + dayOfMonth
        }

        private fun getCalendar(timestamp: Long): GregorianCalendar {
            val day = GregorianCalendar(TimeZone.getTimeZone("GMT"), getLocale())
            day.timeInMillis = timestamp
            return day
        }

        @JvmStatic
        fun getLocalTime(utcTimeInMillis: Long? = null): Long {
            if (fixedLocalTime != null) return fixedLocalTime as Long

            val tz = getTimeZone()
            val now = utcTimeInMillis ?: Date().time
            return now + tz.getOffset(now)
        }

        /**
         * Returns an array of strings with the names for each day of the week,
         * in either SHORT or LONG format. The first entry corresponds to the
         * first day of the week, according to the provided argument.
         *
         * @param format Either GregorianCalendar.SHORT or LONG
         * @param firstWeekDay An integer representing the first day of the week,
         *                     following java.util.Calendar conventions. That is,
         *                     Saturday corresponds to 7, and Sunday corresponds
         *                     to 1.
         */
        private fun getWeekdayNames(
            format: Int,
            firstWeekDay: Int
        ): Array<String> {
            val calendar = GregorianCalendar(getLocale())
            calendar.set(DAY_OF_WEEK, firstWeekDay)

            val daysNullable = ArrayList<String>()
            for (i in 1..7) {
                daysNullable.add(
                    calendar.getDisplayName(
                        DAY_OF_WEEK,
                        format,
                        getLocale()
                    )
                )

                calendar.add(DAY_OF_MONTH, 1)
            }

            return daysNullable.toTypedArray()
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
        @JvmStatic
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

        /**
         * @return An integer representing the first day of the week, according to
         * the current locale. Sunday corresponds to 1, Monday to 2, and so on,
         * until Saturday, which is represented by 7. This is consistent
         * with java.util.Calendar constants.
         */
        @JvmStatic
        fun getFirstWeekdayNumberAccordingToLocale(): Int {
            return GregorianCalendar(getLocale()).firstDayOfWeek
        }

        /**
         * @return A vector of strings with the long names for the week days,
         * according to the current locale. The first entry corresponds to Saturday,
         * the second entry corresponds to Monday, and so on.
         *
         * @param firstWeekday Either Calendar.SATURDAY, Calendar.MONDAY, or other
         *                     weekdays defined in this class.
         */
        @JvmStatic
        fun getLongWeekdayNames(firstWeekday: Int): Array<String> {
            return getWeekdayNames(GregorianCalendar.LONG, firstWeekday)
        }

        /**
         * Returns a vector of strings with the short names for the week days,
         * according to the current locale. The first entry corresponds to Saturday,
         * the second entry corresponds to Monday, and so on.
         *
         * @param firstWeekday Either Calendar.SATURDAY, Calendar.MONDAY, or other
         *                     weekdays defined in this class.
         */
        @JvmStatic
        fun getShortWeekdayNames(firstWeekday: Int): Array<String> {
            return getWeekdayNames(GregorianCalendar.SHORT, firstWeekday)
        }

        @JvmStatic
        fun getToday(): Timestamp = Timestamp(getStartOfToday())

        @JvmStatic
        fun getTodayWithOffset(): Timestamp = Timestamp(getStartOfTodayWithOffset())

        @JvmStatic
        fun getStartOfDay(timestamp: Long): Long = (timestamp / DAY_LENGTH) * DAY_LENGTH

        @JvmStatic
        fun getStartOfDayWithOffset(timestamp: Long): Long {
            val offset = startDayHourOffset * HOUR_LENGTH + startDayMinuteOffset * MINUTE_LENGTH
            return getStartOfDay(timestamp - offset)
        }

        @JvmStatic
        fun getStartOfToday(): Long = getStartOfDay(getLocalTime())

        @JvmStatic
        fun getStartOfTomorrowWithOffset(): Long = getUpcomingTimeInMillis(
            startDayHourOffset,
            startDayMinuteOffset
        )

        @JvmStatic
        fun getStartOfTodayWithOffset(): Long = getStartOfDayWithOffset(getLocalTime())

        @JvmStatic
        fun millisecondsUntilTomorrowWithOffset(): Long = getStartOfTomorrowWithOffset() - getLocalTime()

        @JvmStatic
        fun getStartOfTodayCalendar(): GregorianCalendar = getCalendar(getStartOfToday())

        @JvmStatic
        fun getStartOfTodayCalendarWithOffset(): GregorianCalendar = getCalendar(getStartOfTodayWithOffset())

        private fun getTimeZone(): TimeZone {
            return fixedTimeZone ?: TimeZone.getDefault()
        }

        @JvmStatic
        fun removeTimezone(timestamp: Long): Long {
            val tz = getTimeZone()
            return timestamp + tz.getOffset(timestamp)
        }

        @JvmStatic
        fun setStartDayOffset(hourOffset: Int, minuteOffset: Int) {
            startDayHourOffset = hourOffset
            startDayMinuteOffset = minuteOffset
        }

        private fun getLocale(): Locale {
            return fixedLocale ?: Locale.getDefault()
        }

        @JvmStatic
        fun truncate(
            field: TruncateField,
            timestamp: Timestamp,
            firstWeekday: Int
        ): Timestamp {
            return Timestamp(
                truncate(
                    field,
                    timestamp.unixTime,
                    firstWeekday
                )
            )
        }

        @JvmStatic
        fun truncate(
            field: TruncateField,
            timestamp: Long,
            firstWeekday: Int
        ): Long {
            val cal = getCalendar(timestamp)

            return when (field) {

                TruncateField.DAY -> { cal.timeInMillis }

                TruncateField.MONTH -> {
                    cal.set(DAY_OF_MONTH, 1)
                    cal.timeInMillis
                }

                TruncateField.WEEK_NUMBER -> {
                    val weekDay = cal.get(DAY_OF_WEEK)
                    var delta = weekDay - firstWeekday
                    if (delta < 0) { delta += 7 }
                    cal.add(Calendar.DAY_OF_YEAR, -delta)
                    cal.timeInMillis
                }

                TruncateField.QUARTER -> {
                    val quarter = cal.get(Calendar.MONTH) / 3
                    cal.set(DAY_OF_MONTH, 1)
                    cal.set(Calendar.MONTH, quarter * 3)
                    cal.timeInMillis
                }

                TruncateField.YEAR -> {
                    cal.set(Calendar.MONTH, Calendar.JANUARY)
                    cal.set(DAY_OF_MONTH, 1)
                    cal.timeInMillis
                }
            }
        }

        @JvmStatic
        fun getUpcomingTimeInMillis(
            hour: Int,
            minute: Int
        ): Long {
            val calendar = getStartOfTodayCalendar()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            var time = calendar.timeInMillis

            if (getLocalTime() > time) {
                time += DAY_LENGTH
            }

            return applyTimezone(time)
        }

        @JvmStatic
        fun setFixedLocalTime(newFixedLocalTime: Long?) {
            this.fixedLocalTime = newFixedLocalTime
        }

        @JvmStatic
        fun setFixedTimeZone(newTimeZone: TimeZone?) {
            this.fixedTimeZone = newTimeZone
        }

        @JvmStatic
        fun setFixedLocale(newLocale: Locale?) {
            this.fixedLocale = newLocale
        }
    }

    enum class TruncateField {
        DAY, MONTH, WEEK_NUMBER, YEAR, QUARTER
    }
}
