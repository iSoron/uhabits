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

import junit.framework.Assert.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.applyTimezone
import org.isoron.uhabits.core.utils.DateUtils.Companion.formatHeaderDate
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfDayWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.millisecondsUntilTomorrowWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.removeTimezone
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocalTime
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocale
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedTimeZone
import org.isoron.uhabits.core.utils.DateUtils.Companion.setStartDayOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.truncate
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class DateUtilsTest : BaseUnitTest() {
    var firstWeekday = Calendar.SUNDAY

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        setFixedLocale(Locale.US)
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
        setFixedLocalTime(null)
        setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"))
        val utcTestTimeInMillis = unixTime(2015, Calendar.JANUARY, 11)
        val localTimeInMillis = DateUtils.getLocalTime(utcTestTimeInMillis)
        val expectedUnixTimeOffsetForSydney = 11 * 60 * 60 * 1000
        val expectedUnixTimeForSydney = utcTestTimeInMillis + expectedUnixTimeOffsetForSydney
        assertThat(expectedUnixTimeForSydney, equalTo(localTimeInMillis))
    }

    @Test
    fun testGetWeekdaySequence() {
        val weekdaySequence = DateUtils.getWeekdaySequence(3)
        assertThat(arrayOf(3, 4, 5, 6, 7, 1, 2), equalTo(weekdaySequence))
    }

    @Test
    fun testGetFirstWeekdayNumberAccordingToLocale_germany() {
        setFixedLocale(Locale.GERMANY)
        val firstWeekdayNumber = DateUtils.getFirstWeekdayNumberAccordingToLocale()
        assertThat(2, equalTo(firstWeekdayNumber))
    }

    @Test
    fun testGetFirstWeekdayNumberAccordingToLocale_us() {
        setFixedLocale(Locale.US)
        val firstWeekdayNumber = DateUtils.getFirstWeekdayNumberAccordingToLocale()
        assertThat(1, equalTo(firstWeekdayNumber))
    }

    @Test
    fun testGetLongWeekdayNames_germany() {
        setFixedLocale(Locale.GERMANY)
        val longWeekdayNames = DateUtils.getLongWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Samstag", "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetLongWeekdayNames_us() {
        setFixedLocale(Locale.US)
        val longWeekdayNames = DateUtils.getLongWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetShortWeekdayNames_germany() {
        setFixedLocale(Locale.GERMANY)
        val longWeekdayNames = DateUtils.getShortWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Sa.", "So.", "Mo.", "Di.", "Mi.", "Do.", "Fr."), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetShortWeekdayNames_us() {
        setFixedLocale(Locale.US)
        val longWeekdayNames = DateUtils.getShortWeekdayNames(Calendar.SATURDAY)
        assertThat(arrayOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"), equalTo(longWeekdayNames))
    }

    @Test
    fun testGetToday() {
        setFixedLocalTime(FIXED_LOCAL_TIME)
        val today = DateUtils.getToday()
        assertThat(Timestamp(FIXED_LOCAL_TIME), equalTo(today))
    }

    @Test
    fun testGetStartOfDay() {
        val expectedStartOfDayUtc = fixedStartOfToday()
        val laterInTheDayUtc = fixedStartOfTodayWithOffset(20)
        val startOfDay = DateUtils.getStartOfDay(laterInTheDayUtc)
        assertThat(expectedStartOfDayUtc, equalTo(startOfDay))
    }

    @Test
    fun testGetStartOfToday() {
        val expectedStartOfDayUtc = fixedStartOfToday()
        val laterInTheDayUtc = fixedStartOfTodayWithOffset(20)
        setFixedLocalTime(laterInTheDayUtc)
        val startOfToday = DateUtils.getStartOfToday()
        assertThat(expectedStartOfDayUtc, equalTo(startOfToday))
    }

    @Test
    fun testGetStartOfTomorrowWithOffset_priorToOffset() {
        val priorToOffset = HOUR_OFFSET - 1
        testGetStartOfTomorrowWithOffset(priorToOffset)
    }

    @Test
    fun testGetStartOfTomorrowWithOffset_afterOffset() {
        val afterOffset = HOUR_OFFSET + 1 - HOURS_IN_ONE_DAY
        testGetStartOfTomorrowWithOffset(afterOffset)
    }

    private fun testGetStartOfTomorrowWithOffset(startOfTodayOffset: Int) {
        configureOffsetTest(startOfTodayOffset)
        assertThat(
            fixedStartOfTodayWithOffset(HOUR_OFFSET),
            equalTo(DateUtils.getStartOfTomorrowWithOffset())
        )
    }

    @Test
    fun testGetStartOfTodayWithOffset_priorToOffset() {
        val priorToOffset = HOURS_IN_ONE_DAY + HOUR_OFFSET - 1
        testGetStartOfTodayWithOffset(priorToOffset)
    }

    @Test
    fun testGetStartOfTodayWithOffset_afterOffset() {
        val afterOffset = HOUR_OFFSET + 1
        testGetStartOfTodayWithOffset(afterOffset)
    }

    private fun testGetStartOfTodayWithOffset(startOfTodayOffset: Int) {
        configureOffsetTest(startOfTodayOffset)
        assertThat(
            fixedStartOfToday(),
            equalTo(DateUtils.getStartOfTodayWithOffset())
        )
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
    fun testGetUpcomingTimeInMillis() {
        setFixedLocalTime(FIXED_LOCAL_TIME)
        setFixedTimeZone(TimeZone.getTimeZone("GMT"))
        val expected = unixTime(2015, Calendar.JANUARY, 25, 10, 1)
        val upcomingTimeMillis = DateUtils.getUpcomingTimeInMillis(10, 1)
        assertThat(expected, equalTo(upcomingTimeMillis))
    }

    @Test
    @Throws(Exception::class)
    fun testMillisecondsUntilTomorrow() {
        setFixedTimeZone(TimeZone.getTimeZone("GMT"))
        setFixedLocalTime(unixTime(2017, Calendar.JANUARY, 1, 23, 59))
        assertThat(millisecondsUntilTomorrowWithOffset(), equalTo(DateUtils.MINUTE_LENGTH))
        setFixedLocalTime(fixedStartOfTodayWithOffset(20))
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(4 * DateUtils.HOUR_LENGTH)
        )
        setStartDayOffset(HOUR_OFFSET, 30)
        setFixedLocalTime(unixTime(2017, Calendar.JANUARY, 1, 23, 59))
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(HOUR_OFFSET * DateUtils.HOUR_LENGTH + 31 * DateUtils.MINUTE_LENGTH)
        )
        setFixedLocalTime(unixTime(2017, Calendar.JANUARY, 2, 1, 0))
        assertThat(
            millisecondsUntilTomorrowWithOffset(),
            equalTo(2 * DateUtils.HOUR_LENGTH + 30 * DateUtils.MINUTE_LENGTH)
        )
    }

    @Test
    fun testGetStartOfTodayCalendar() {
        setFixedLocalTime(FIXED_LOCAL_TIME)
        setFixedLocale(Locale.GERMANY)
        val expectedStartOfDay = unixTime(2015, Calendar.JANUARY, 25, 0, 0)
        val expectedCalendar = GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.GERMANY)
        expectedCalendar.timeInMillis = expectedStartOfDay
        val startOfTodayCalendar = DateUtils.getStartOfTodayCalendar()
        assertThat(expectedCalendar, equalTo(startOfTodayCalendar))
    }

    @Test
    fun testGetStartOfTodayCalendarWithOffset_priorToOffset() {
        val priorToOffset = HOUR_OFFSET - 1
        testGetStartOfTodayCalendarWithOffset(priorToOffset)
    }

    @Test
    fun testGetStartOfTodayCalendarWithOffset_afterOffset() {
        val afterOffset = HOUR_OFFSET + 1
        testGetStartOfTodayCalendarWithOffset(afterOffset)
    }

    private fun testGetStartOfTodayCalendarWithOffset(startOfTodayOffset: Int) {
        configureOffsetTest(startOfTodayOffset)
        setFixedLocale(Locale.GERMANY)
        val expectedCalendar = GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.GERMANY)
        expectedCalendar.timeInMillis = fixedStartOfToday()
        assertThat(
            expectedCalendar,
            equalTo(DateUtils.getStartOfTodayCalendar())
        )
    }

    private fun configureOffsetTest(startOfTodayOffset: Int) {
        setStartDayOffset(HOUR_OFFSET, 0)
        setFixedTimeZone(TimeZone.getTimeZone("GMT"))
        setFixedLocalTime(fixedStartOfTodayWithOffset(startOfTodayOffset))
    }

    private fun fixedStartOfToday() = fixedStartOfTodayWithOffset(0)

    private fun fixedStartOfTodayWithOffset(hourOffset: Int): Long {
        return unixTime(2017, Calendar.JANUARY, 1, hourOffset, 0)
    }

    @Test
    @Throws(Exception::class)
    fun testGetTodayWithOffset() {
        assertThat(getTodayWithOffset(), equalTo(Timestamp(FIXED_LOCAL_TIME)))
        setStartDayOffset(9, 0)
        assertThat(
            getTodayWithOffset(),
            equalTo(Timestamp(FIXED_LOCAL_TIME - DateUtils.DAY_LENGTH))
        )
    }

    @Test
    @Throws(Exception::class)
    fun testGetStartOfDayWithOffset() {
        val timestamp = unixTime(2020, Calendar.SEPTEMBER, 3)
        assertThat(
            getStartOfDayWithOffset(timestamp + DateUtils.HOUR_LENGTH),
            equalTo(timestamp)
        )
        setStartDayOffset(3, 30)
        assertThat(
            getStartOfDayWithOffset(timestamp + 3 * DateUtils.HOUR_LENGTH + 29 * DateUtils.MINUTE_LENGTH),
            equalTo(timestamp - DateUtils.DAY_LENGTH)
        )
    }

    @Test
    fun test_applyTimezone() {
        setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"))
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.JULY, 30, 18, 0)),
            unixTime(2017, Calendar.JULY, 30, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 0, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 29, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 10, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 11, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 12, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 2, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 13, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 3, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 22, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 12, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 23, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 0, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 1, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 15, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 1, 59)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 15, 59)
        )
        // DST begins
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 3, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 16, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 4, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 17, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 5, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 18, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 11, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 12, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 13, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 2, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 14, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 3, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 15, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 4, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 1, 19, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.OCTOBER, 2, 19, 0)),
            unixTime(2017, Calendar.OCTOBER, 2, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2017, Calendar.NOVEMBER, 30, 19, 0)),
            unixTime(2017, Calendar.NOVEMBER, 30, 8, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.MARCH, 31, 0, 0)),
            unixTime(2018, Calendar.MARCH, 30, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.MARCH, 31, 12, 0)),
            unixTime(2018, Calendar.MARCH, 31, 1, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.MARCH, 31, 18, 0)),
            unixTime(2018, Calendar.MARCH, 31, 7, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 0, 0)),
            unixTime(2018, Calendar.MARCH, 31, 13, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 1, 0)),
            unixTime(2018, Calendar.MARCH, 31, 14, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 1, 59)),
            unixTime(2018, Calendar.MARCH, 31, 14, 59)
        )
        // DST ends
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 2, 0)),
            unixTime(2018, Calendar.MARCH, 31, 16, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 3, 0)),
            unixTime(2018, Calendar.MARCH, 31, 17, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 4, 0)),
            unixTime(2018, Calendar.MARCH, 31, 18, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 10, 0)),
            unixTime(2018, Calendar.APRIL, 1, 0, 0)
        )
        assertEquals(
            applyTimezone(unixTime(2018, Calendar.APRIL, 1, 18, 0)),
            unixTime(2018, Calendar.APRIL, 1, 8, 0)
        )
    }

    @Test
    fun test_removeTimezone() {
        setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"))
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.JULY, 30, 8, 0)),
            unixTime(2017, Calendar.JULY, 30, 18, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 29, 14, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 0, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 10, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 1, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 11, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 2, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 3, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 13, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 12, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 22, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 13, 0)),
            unixTime(2017, Calendar.SEPTEMBER, 30, 23, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 14, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 15, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 1, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 15, 59)),
            unixTime(2017, Calendar.OCTOBER, 1, 1, 59)
        )
        // DST begins
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 16, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 3, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 17, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 4, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.SEPTEMBER, 30, 18, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 5, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 0, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 11, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 1, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 2, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 13, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 3, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 14, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 4, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 15, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 1, 8, 0)),
            unixTime(2017, Calendar.OCTOBER, 1, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.OCTOBER, 2, 8, 0)),
            unixTime(2017, Calendar.OCTOBER, 2, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2017, Calendar.NOVEMBER, 30, 8, 0)),
            unixTime(2017, Calendar.NOVEMBER, 30, 19, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 30, 13, 0)),
            unixTime(2018, Calendar.MARCH, 31, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 1, 0)),
            unixTime(2018, Calendar.MARCH, 31, 12, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 7, 0)),
            unixTime(2018, Calendar.MARCH, 31, 18, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 13, 0)),
            unixTime(2018, Calendar.APRIL, 1, 0, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 14, 0)),
            unixTime(2018, Calendar.APRIL, 1, 1, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 14, 59)),
            unixTime(2018, Calendar.APRIL, 1, 1, 59)
        )
        // DST ends
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 16, 0)),
            unixTime(2018, Calendar.APRIL, 1, 2, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 17, 0)),
            unixTime(2018, Calendar.APRIL, 1, 3, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.MARCH, 31, 18, 0)),
            unixTime(2018, Calendar.APRIL, 1, 4, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.APRIL, 1, 0, 0)),
            unixTime(2018, Calendar.APRIL, 1, 10, 0)
        )
        assertEquals(
            removeTimezone(unixTime(2018, Calendar.APRIL, 1, 8, 0)),
            unixTime(2018, Calendar.APRIL, 1, 18, 0)
        )
    }

    companion object {
        const val HOUR_OFFSET = 3
        const val HOURS_IN_ONE_DAY = 24
    }
}
