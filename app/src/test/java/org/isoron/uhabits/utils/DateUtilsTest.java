/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.utils;

import org.isoron.uhabits.*;
import org.junit.*;

import java.util.*;

import static java.util.Calendar.*;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.isoron.uhabits.utils.DateUtils.applyTimezone;
import static org.isoron.uhabits.utils.DateUtils.removeTimezone;

public class DateUtilsTest extends BaseUnitTest
{
    @Test
    public void testFormatHeaderDate()
    {
        long timestamp = timestamp(2015, DECEMBER, 31);
        GregorianCalendar date = DateUtils.getCalendar(timestamp);
        String formatted = DateUtils.formatHeaderDate(date);
        assertThat(formatted, equalTo("Thu\n31"));
    }

    @Test
    public void testTruncate_dayOfWeek()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.WEEK_NUMBER;

        long expected = timestamp(2015, Calendar.JANUARY, 11);
        long t0 = timestamp(2015, Calendar.JANUARY, 11);
        long t1 = timestamp(2015, Calendar.JANUARY, 16);
        long t2 = timestamp(2015, Calendar.JANUARY, 17);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2015, Calendar.JANUARY, 18);
        t0 = timestamp(2015, Calendar.JANUARY, 18);
        t1 = timestamp(2015, Calendar.JANUARY, 19);
        t2 = timestamp(2015, Calendar.JANUARY, 24);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_month()
    {
        long expected = timestamp(2016, Calendar.JUNE, 1);
        long t0 = timestamp(2016, Calendar.JUNE, 1);
        long t1 = timestamp(2016, Calendar.JUNE, 15);
        long t2 = timestamp(2016, Calendar.JUNE, 20);

        DateUtils.TruncateField field = DateUtils.TruncateField.MONTH;

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2016, DECEMBER, 1);
        t0 = timestamp(2016, DECEMBER, 1);
        t1 = timestamp(2016, DECEMBER, 15);
        t2 = timestamp(2016, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_quarter()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.QUARTER;

        long expected = timestamp(2016, JANUARY, 1);
        long t0 = timestamp(2016, JANUARY, 20);
        long t1 = timestamp(2016, FEBRUARY, 15);
        long t2 = timestamp(2016, MARCH, 30);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2016, APRIL, 1);
        t0 = timestamp(2016, APRIL, 1);
        t1 = timestamp(2016, MAY, 30);
        t2 = timestamp(2016, JUNE, 20);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_year()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.YEAR;

        long expected = timestamp(2016, JANUARY, 1);
        long t0 = timestamp(2016, JANUARY, 1);
        long t1 = timestamp(2016, FEBRUARY, 25);
        long t2 = timestamp(2016, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2017, JANUARY, 1);
        t0 = timestamp(2017, JANUARY, 1);
        t1 = timestamp(2017, MAY, 30);
        t2 = timestamp(2017, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void test_getDaysBetween()
    {
        long t1 = timestamp(2016, JANUARY, 1);
        long t2 = timestamp(2016, JANUARY, 10);
        long t3 = timestamp(2016, DECEMBER, 31);

        assertThat(DateUtils.getDaysBetween(t1, t1), equalTo(0));
        assertThat(DateUtils.getDaysBetween(t1, t2), equalTo(9));
        assertThat(DateUtils.getDaysBetween(t1, t3), equalTo(365));
        assertThat(DateUtils.getDaysBetween(t3, t1), equalTo(365));
    }

    @Test
    public void test_applyTimezone()
    {
        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        assertEquals(applyTimezone(timestamp(2017, JULY, 30, 18, 0, 0)), (timestamp(2017, JULY, 30, 8, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30,  0, 0, 0)), (timestamp(2017, SEPTEMBER, 29, 14, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 10, 0, 0)), (timestamp(2017, SEPTEMBER, 30,  0, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 11, 0, 0)), (timestamp(2017, SEPTEMBER, 30,  1, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 12, 0, 0)), (timestamp(2017, SEPTEMBER, 30,  2, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 13, 0, 0)), (timestamp(2017, SEPTEMBER, 30,  3, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 22, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 12, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, SEPTEMBER, 30, 23, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 13, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  0,  0, 0)), (timestamp(2017, SEPTEMBER, 30, 14,  0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  1,  0, 0)), (timestamp(2017, SEPTEMBER, 30, 15,  0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  1, 59, 0)), (timestamp(2017, SEPTEMBER, 30, 15, 59, 0)));
        // DST begins
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  3,  0, 0)), (timestamp(2017, SEPTEMBER, 30, 16,  0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  4,  0, 0)), (timestamp(2017, SEPTEMBER, 30, 17,  0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1,  5,  0, 0)), (timestamp(2017, SEPTEMBER, 30, 18,  0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 11,  0, 0)), (timestamp(2017, OCTOBER, 1, 0, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 12,  0, 0)), (timestamp(2017, OCTOBER, 1, 1, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 13,  0, 0)), (timestamp(2017, OCTOBER, 1, 2, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 14,  0, 0)), (timestamp(2017, OCTOBER, 1, 3, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 15,  0, 0)), (timestamp(2017, OCTOBER, 1, 4, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 1, 19,  0, 0)), (timestamp(2017, OCTOBER, 1, 8, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, OCTOBER, 2, 19, 0, 0)), (timestamp(2017, OCTOBER, 2, 8, 0, 0)));
        assertEquals(applyTimezone(timestamp(2017, NOVEMBER, 30, 19, 0, 0)), (timestamp(2017, NOVEMBER, 30, 8, 0, 0)));
        assertEquals(applyTimezone(timestamp(2018, MARCH, 31,  0,  0, 0)), (timestamp(2018, MARCH, 30, 13,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, MARCH, 31, 12,  0, 0)), (timestamp(2018, MARCH, 31,  1,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, MARCH, 31, 18,  0, 0)), (timestamp(2018, MARCH, 31,  7,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  0,  0, 0)), (timestamp(2018, MARCH, 31, 13,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  1,  0, 0)), (timestamp(2018, MARCH, 31, 14,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  1, 59, 0)), (timestamp(2018, MARCH, 31, 14, 59, 0)));
        // DST ends
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  2,  0, 0)), (timestamp(2018, MARCH, 31, 16,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  3,  0, 0)), (timestamp(2018, MARCH, 31, 17,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1,  4,  0, 0)), (timestamp(2018, MARCH, 31, 18,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1, 10,  0, 0)), (timestamp(2018, APRIL,  1,  0,  0, 0)));
        assertEquals(applyTimezone(timestamp(2018, APRIL,  1, 18,  0, 0)), (timestamp(2018, APRIL,  1,  8,  0, 0)));
    }

    @Test
    public void test_removeTimezone()
    {
        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        assertEquals(removeTimezone(timestamp(2017, JULY, 30, 8, 0, 0)), (timestamp(2017, JULY, 30, 18, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 29, 14, 0, 0)), (timestamp(2017, SEPTEMBER, 30,  0, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30,  0, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 10, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30,  1, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 11, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30,  2, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 12, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30,  3, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 13, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 12, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 22, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 13, 0, 0)), (timestamp(2017, SEPTEMBER, 30, 23, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 14,  0, 0)), (timestamp(2017, OCTOBER, 1,  0,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 15,  0, 0)), (timestamp(2017, OCTOBER, 1,  1,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 15, 59, 0)), (timestamp(2017, OCTOBER, 1,  1, 59, 0)));
        // DST begins
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 16,  0, 0)), (timestamp(2017, OCTOBER, 1,  3,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 17,  0, 0)), (timestamp(2017, OCTOBER, 1,  4,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, SEPTEMBER, 30, 18,  0, 0)), (timestamp(2017, OCTOBER, 1,  5,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 0, 0, 0)), (timestamp(2017, OCTOBER, 1, 11,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 1, 0, 0)), (timestamp(2017, OCTOBER, 1, 12,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 2, 0, 0)), (timestamp(2017, OCTOBER, 1, 13,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 3, 0, 0)), (timestamp(2017, OCTOBER, 1, 14,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 4, 0, 0)), (timestamp(2017, OCTOBER, 1, 15,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 1, 8, 0, 0)), (timestamp(2017, OCTOBER, 1, 19,  0, 0)));
        assertEquals(removeTimezone(timestamp(2017, OCTOBER, 2, 8, 0, 0)), (timestamp(2017, OCTOBER, 2, 19, 0, 0)));
        assertEquals(removeTimezone(timestamp(2017, NOVEMBER, 30, 8, 0, 0)), (timestamp(2017, NOVEMBER, 30, 19, 0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 30, 13,  0, 0)), (timestamp(2018, MARCH, 31,  0,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31,  1,  0, 0)), (timestamp(2018, MARCH, 31, 12,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31,  7,  0, 0)), (timestamp(2018, MARCH, 31, 18,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 13,  0, 0)), (timestamp(2018, APRIL,  1,  0,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 14,  0, 0)), (timestamp(2018, APRIL,  1,  1,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 14, 59, 0)), (timestamp(2018, APRIL,  1,  1, 59, 0)));
        // DST ends
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 16,  0, 0)), (timestamp(2018, APRIL,  1,  2,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 17,  0, 0)), (timestamp(2018, APRIL,  1,  3,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, MARCH, 31, 18,  0, 0)), (timestamp(2018, APRIL,  1,  4,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, APRIL,  1,  0,  0, 0)), (timestamp(2018, APRIL,  1, 10,  0, 0)));
        assertEquals(removeTimezone(timestamp(2018, APRIL,  1,  8,  0, 0)), (timestamp(2018, APRIL,  1, 18,  0, 0)));
    }
}
