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

import io.fluidsonic.locale.Locale
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.platform.core.BaseUnitTest.Companion.FIXED_LOCAL_TIME
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.LocalDate.Companion.DAY_LENGTH
import org.isoron.platform.time.LocalDate.Companion.HOUR_LENGTH
import org.isoron.platform.time.LocalDate.Companion.MINUTE_LENGTH
import org.isoron.platform.time.LocalDate.Companion.setStartDayOffset
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.formatHeaderDate
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.millisecondsUntilTomorrowWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.truncate
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class DateUtilsTest : BaseUnitTest() {
    var firstWeekday = Calendar.SUNDAY

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        LocalDate.fixedLocale = Locale.forLanguageTag("en-us")
    }

    @Test
    fun testFormatHeaderDate() {
        val timestamp = unixTime(2015, Calendar.DECEMBER, 31)
        val date = Timestamp(timestamp).toCalendar()
        val formatted = formatHeaderDate(date)
        assertThat(formatted, equalTo("Thu\n31"))
    }

    @Test
    fun testGetLocalTime() {
        LocalDate.fixedLocalTime = null
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.of("Australia/Sydney")
        val utcTestTimeInMillis = unixTime(2015, Calendar.JANUARY, 11)
        val localTimeInMillis = LocalDate.getLocalTime(utcTestTimeInMillis)
        val expectedUnixTimeOffsetForSydney = 11 * 60 * 60 * 1000
        val expectedUnixTimeForSydney = utcTestTimeInMillis + expectedUnixTimeOffsetForSydney
        assertThat(expectedUnixTimeForSydney, equalTo(localTimeInMillis))
    }

    @Test
    fun testGetFirstWeekdayNumberAccordingToLocale_germany() {
        LocalDate.fixedLocale = Locale.forLanguageTag("de-de")
        val firstWeekdayNumber = DateUtils.getFirstWeekdayNumberAccordingToLocale()
        assertThat(2, equalTo(firstWeekdayNumber))
    }

    @Test
    fun testGetFirstWeekdayNumberAccordingToLocale_us() {
        LocalDate.fixedLocale = Locale.forLanguageTag("en-us")
        val firstWeekdayNumber = DateUtils.getFirstWeekdayNumberAccordingToLocale()
        assertThat(1, equalTo(firstWeekdayNumber))
    }

    @Test
    fun testGetLongWeekdayNames_germany() {
        LocalDate.fixedLocale = Locale.forLanguageTag("de-de")
        val longWeekdayNames = DateUtils.getLongWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Samstag", "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetLongWeekdayNames_us() {
        LocalDate.fixedLocale = Locale.forLanguageTag("en-us")
        val longWeekdayNames = DateUtils.getLongWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetShortWeekdayNames_germany() {
        LocalDate.fixedLocale = Locale.forLanguageTag("de-de")
        val longWeekdayNames = DateUtils.getShortWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Sa.", "So.", "Mo.", "Di.", "Mi.", "Do.", "Fr."), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetShortWeekdayNames_us() {
        LocalDate.fixedLocale = Locale.forLanguageTag("en-us")
        val longWeekdayNames = DateUtils.getShortWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetToday() {
        LocalDate.fixedLocalTime = FIXED_LOCAL_TIME
        val today = DateUtils.getToday()
        assertThat(Timestamp(FIXED_LOCAL_TIME), equalTo(today))
    }

    @Test
    fun testGetStartOfTomorrowWithOffset_priorToOffset() {
        val hourOffset = 3
        setStartDayOffset(hourOffset, 0)
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        val startOfTomorrowWithOffset = unixTime(2017, Calendar.JANUARY, 1, hourOffset, 0)
        val priorToOffset = unixTime(2017, Calendar.JANUARY, 1, hourOffset - 1, 0)
        LocalDate.fixedLocalTime = priorToOffset
        val startOfTomorrow = DateUtils.getStartOfTomorrowWithOffset()
        assertThat(startOfTomorrowWithOffset, equalTo(startOfTomorrow))
    }

    @Test
    fun testGetStartOfTomorrowWithOffset_afterOffset() {
        val hourOffset = 3
        setStartDayOffset(hourOffset, 0)
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        val startOfTomorrowWithOffset = unixTime(2017, Calendar.JANUARY, 2, hourOffset, 0)
        val afterOffset = unixTime(2017, Calendar.JANUARY, 1, hourOffset + 1, 0)
        LocalDate.fixedLocalTime = afterOffset
        val startOfTomorrow = DateUtils.getStartOfTomorrowWithOffset()
        assertThat(startOfTomorrowWithOffset, equalTo(startOfTomorrow))
    }

    @Test
    fun testTruncate_dayOfWeek() {
        val field = DateUtils.TruncateField.WEEK_NUMBER
        var expected = unixTime(2015, Calendar.JANUARY, 11)
        var t0 = unixTime(2015, Calendar.JANUARY, 11)
        var t1 = unixTime(2015, Calendar.JANUARY, 16)
        var t2 = unixTime(2015, Calendar.JANUARY, 17)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
        expected = unixTime(2015, Calendar.JANUARY, 18)
        t0 = unixTime(2015, Calendar.JANUARY, 18)
        t1 = unixTime(2015, Calendar.JANUARY, 19)
        t2 = unixTime(2015, Calendar.JANUARY, 24)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
        firstWeekday = Calendar.WEDNESDAY
        expected = unixTime(2015, Calendar.JANUARY, 7)
        t0 = unixTime(2015, Calendar.JANUARY, 7)
        t1 = unixTime(2015, Calendar.JANUARY, 9)
        t2 = unixTime(2015, Calendar.JANUARY, 13)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
    }

    @Test
    fun testTruncate_month() {
        var expected = unixTime(2016, Calendar.JUNE, 1)
        var t0 = unixTime(2016, Calendar.JUNE, 1)
        var t1 = unixTime(2016, Calendar.JUNE, 15)
        var t2 = unixTime(2016, Calendar.JUNE, 20)
        val field = DateUtils.TruncateField.MONTH
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
        expected = unixTime(2016, Calendar.DECEMBER, 1)
        t0 = unixTime(2016, Calendar.DECEMBER, 1)
        t1 = unixTime(2016, Calendar.DECEMBER, 15)
        t2 = unixTime(2016, Calendar.DECEMBER, 31)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
    }

    @Test
    fun testTruncate_quarter() {
        val field = DateUtils.TruncateField.QUARTER
        var expected = unixTime(2016, Calendar.JANUARY, 1)
        var t0 = unixTime(2016, Calendar.JANUARY, 20)
        var t1 = unixTime(2016, Calendar.FEBRUARY, 15)
        var t2 = unixTime(2016, Calendar.MARCH, 30)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
        expected = unixTime(2016, Calendar.APRIL, 1)
        t0 = unixTime(2016, Calendar.APRIL, 1)
        t1 = unixTime(2016, Calendar.MAY, 30)
        t2 = unixTime(2016, Calendar.JUNE, 20)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
    }

    @Test
    fun testTruncate_year() {
        val field = DateUtils.TruncateField.YEAR
        var expected = unixTime(2016, Calendar.JANUARY, 1)
        var t0 = unixTime(2016, Calendar.JANUARY, 1)
        var t1 = unixTime(2016, Calendar.FEBRUARY, 25)
        var t2 = unixTime(2016, Calendar.DECEMBER, 31)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
        expected = unixTime(2017, Calendar.JANUARY, 1)
        t0 = unixTime(2017, Calendar.JANUARY, 1)
        t1 = unixTime(2017, Calendar.MAY, 30)
        t2 = unixTime(2017, Calendar.DECEMBER, 31)
        assertThat(truncate(field, t0, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t1, firstWeekday), equalTo(expected))
        assertThat(truncate(field, t2, firstWeekday), equalTo(expected))
    }

    @Test
    fun testTruncate_timestamp() {
        val field = DateUtils.TruncateField.YEAR
        val nonTruncatedDate = unixTime(2016, Calendar.MAY, 30)
        val expected = Timestamp(unixTime(2016, Calendar.JANUARY, 1))
        assertThat(expected, equalTo(truncate(field, Timestamp(nonTruncatedDate), firstWeekday)))
    }

    @Test
    @Throws(Exception::class)
    fun testMillisecondsUntilTomorrow() {
        LocalDate.fixedTimeZone = kotlinx.datetime.TimeZone.UTC
        LocalDate.fixedLocalTime = unixTime(2017, Calendar.JANUARY, 1, 23, 59)
        assertThat(millisecondsUntilTomorrowWithOffset(), equalTo(MINUTE_LENGTH))
        LocalDate.fixedLocalTime = unixTime(2017, Calendar.JANUARY, 1, 20, 0)
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(4 * HOUR_LENGTH)
        )
        setStartDayOffset(3, 30)
        LocalDate.fixedLocalTime = unixTime(2017, Calendar.JANUARY, 1, 23, 59)
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(3 * HOUR_LENGTH + 31 * MINUTE_LENGTH)
        )
        LocalDate.fixedLocalTime = unixTime(2017, Calendar.JANUARY, 2, 1, 0)
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(2 * HOUR_LENGTH + 30 * MINUTE_LENGTH)
        )
    }

    @Test
    fun testGetStartOfTodayCalendarWithOffset_priorToOffset() {
        val hourOffset = 3
        setStartDayOffset(hourOffset, 0)
        val priorToOffset = unixTime(2017, Calendar.JANUARY, 2, hourOffset - 1, 0)
        LocalDate.fixedLocalTime = priorToOffset
        LocalDate.fixedLocale = Locale.forLanguageTag("de-de")
        val startOfYesterday = unixTime(2017, Calendar.JANUARY, 2, 0, 0)
        val expectedCalendar = GregorianCalendar(TimeZone.getTimeZone("GMT"), java.util.Locale.GERMANY)
        expectedCalendar.timeInMillis = startOfYesterday
        val startOfTodayCalendar = DateUtils.getStartOfTodayCalendar()
        assertThat(expectedCalendar, equalTo(startOfTodayCalendar))
    }

    @Test
    fun testGetStartOfTodayCalendarWithOffset_afterOffset() {
        val hourOffset = 3
        setStartDayOffset(hourOffset, 0)
        val afterOffset = unixTime(2017, Calendar.JANUARY, 1, hourOffset + 1, 0)
        LocalDate.fixedLocalTime = afterOffset
        LocalDate.fixedLocale = Locale.forLanguageTag("de-de")
        val startOfToday = unixTime(2017, Calendar.JANUARY, 1, 0, 0)
        val expectedCalendar = GregorianCalendar(TimeZone.getTimeZone("GMT"), java.util.Locale.GERMANY)
        expectedCalendar.timeInMillis = startOfToday
        val startOfTodayCalendar = DateUtils.getStartOfTodayCalendar()
        assertThat(expectedCalendar, equalTo(startOfTodayCalendar))
    }

    @Test
    @Throws(Exception::class)
    fun testGetTodayWithOffset() {
        assertThat(getTodayWithOffset(), equalTo(Timestamp(FIXED_LOCAL_TIME)))
        setStartDayOffset(9, 0)
        assertThat(
            getTodayWithOffset(),
            equalTo(Timestamp(FIXED_LOCAL_TIME - DAY_LENGTH))
        )
    }
}
