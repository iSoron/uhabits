/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.utils;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;

import java.util.*;

import static java.util.Calendar.*;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.isoron.uhabits.core.utils.DateUtils.applyTimezone;
import static org.isoron.uhabits.core.utils.DateUtils.removeTimezone;

public class DateUtilsTest extends BaseUnitTest
{
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        DateUtils.setFixedLocale(Locale.US);
    }

    @Test
    public void testFormatHeaderDate()
    {
        long timestamp = unixTime(2015, DECEMBER, 31);
        GregorianCalendar date = new Timestamp(timestamp).toCalendar();
        String formatted = DateUtils.formatHeaderDate(date);
        assertThat(formatted, equalTo("Thu\n31"));
    }

    @Test
    public void testTruncate_dayOfWeek()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.WEEK_NUMBER;

        long expected = unixTime(2015, Calendar.JANUARY, 11);
        long t0 = unixTime(2015, Calendar.JANUARY, 11);
        long t1 = unixTime(2015, Calendar.JANUARY, 16);
        long t2 = unixTime(2015, Calendar.JANUARY, 17);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = unixTime(2015, Calendar.JANUARY, 18);
        t0 = unixTime(2015, Calendar.JANUARY, 18);
        t1 = unixTime(2015, Calendar.JANUARY, 19);
        t2 = unixTime(2015, Calendar.JANUARY, 24);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_month()
    {
        long expected = unixTime(2016, Calendar.JUNE, 1);
        long t0 = unixTime(2016, Calendar.JUNE, 1);
        long t1 = unixTime(2016, Calendar.JUNE, 15);
        long t2 = unixTime(2016, Calendar.JUNE, 20);

        DateUtils.TruncateField field = DateUtils.TruncateField.MONTH;

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = unixTime(2016, DECEMBER, 1);
        t0 = unixTime(2016, DECEMBER, 1);
        t1 = unixTime(2016, DECEMBER, 15);
        t2 = unixTime(2016, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_quarter()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.QUARTER;

        long expected = unixTime(2016, JANUARY, 1);
        long t0 = unixTime(2016, JANUARY, 20);
        long t1 = unixTime(2016, FEBRUARY, 15);
        long t2 = unixTime(2016, MARCH, 30);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = unixTime(2016, APRIL, 1);
        t0 = unixTime(2016, APRIL, 1);
        t1 = unixTime(2016, MAY, 30);
        t2 = unixTime(2016, JUNE, 20);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_year()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.YEAR;

        long expected = unixTime(2016, JANUARY, 1);
        long t0 = unixTime(2016, JANUARY, 1);
        long t1 = unixTime(2016, FEBRUARY, 25);
        long t2 = unixTime(2016, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = unixTime(2017, JANUARY, 1);
        t0 = unixTime(2017, JANUARY, 1);
        t1 = unixTime(2017, MAY, 30);
        t2 = unixTime(2017, DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testMillisecondsUntilTomorrow() throws Exception
    {
        DateUtils.setFixedLocalTime(unixTime(2017, JANUARY, 1, 2, 59));
        assertThat(DateUtils.millisecondsUntilTomorrow(), equalTo(60000L));

        DateUtils.setFixedLocalTime(unixTime(2017, JANUARY, 1, 23, 0));
        assertThat(DateUtils.millisecondsUntilTomorrow(), equalTo(14400000L));

    }

    @Test
    public void test_applyTimezone()
    {
        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        assertEquals(applyTimezone(unixTime(2017, JULY, 30, 18, 0)), (unixTime(2017, JULY, 30, 8, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30,  0, 0)), (unixTime(2017, SEPTEMBER, 29, 14, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 10, 0)), (unixTime(2017, SEPTEMBER, 30,  0, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 11, 0)), (unixTime(2017, SEPTEMBER, 30,  1, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 12, 0)), (unixTime(2017, SEPTEMBER, 30,  2, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 13, 0)), (unixTime(2017, SEPTEMBER, 30,  3, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 22, 0)), (unixTime(2017, SEPTEMBER, 30, 12, 0)));
        assertEquals(applyTimezone(unixTime(2017, SEPTEMBER, 30, 23, 0)), (unixTime(2017, SEPTEMBER, 30, 13, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  0,  0)), (unixTime(2017, SEPTEMBER, 30, 14,  0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  1,  0)), (unixTime(2017, SEPTEMBER, 30, 15,  0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  1, 59)), (unixTime(2017, SEPTEMBER, 30, 15, 59)));
        // DST begins
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  3,  0)), (unixTime(2017, SEPTEMBER, 30, 16,  0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  4,  0)), (unixTime(2017, SEPTEMBER, 30, 17,  0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1,  5,  0)), (unixTime(2017, SEPTEMBER, 30, 18,  0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 11,  0)), (unixTime(2017, OCTOBER, 1, 0, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 12,  0)), (unixTime(2017, OCTOBER, 1, 1, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 13,  0)), (unixTime(2017, OCTOBER, 1, 2, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 14,  0)), (unixTime(2017, OCTOBER, 1, 3, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 15,  0)), (unixTime(2017, OCTOBER, 1, 4, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 1, 19,  0)), (unixTime(2017, OCTOBER, 1, 8, 0)));
        assertEquals(applyTimezone(unixTime(2017, OCTOBER, 2, 19, 0)), (unixTime(2017, OCTOBER, 2, 8, 0)));
        assertEquals(applyTimezone(unixTime(2017, NOVEMBER, 30, 19, 0)), (unixTime(2017, NOVEMBER, 30, 8, 0)));
        assertEquals(applyTimezone(unixTime(2018, MARCH, 31,  0,  0)), (unixTime(2018, MARCH, 30, 13,  0)));
        assertEquals(applyTimezone(unixTime(2018, MARCH, 31, 12,  0)), (unixTime(2018, MARCH, 31,  1,  0)));
        assertEquals(applyTimezone(unixTime(2018, MARCH, 31, 18,  0)), (unixTime(2018, MARCH, 31,  7,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  0,  0)), (unixTime(2018, MARCH, 31, 13,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  1,  0)), (unixTime(2018, MARCH, 31, 14,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  1, 59)), (unixTime(2018, MARCH, 31, 14, 59)));
        // DST ends
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  2,  0)), (unixTime(2018, MARCH, 31, 16,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  3,  0)), (unixTime(2018, MARCH, 31, 17,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1,  4,  0)), (unixTime(2018, MARCH, 31, 18,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1, 10,  0)), (unixTime(2018, APRIL,  1,  0,  0)));
        assertEquals(applyTimezone(unixTime(2018, APRIL,  1, 18,  0)), (unixTime(2018, APRIL,  1,  8,  0)));
    }

    @Test
    public void test_removeTimezone()
    {
        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        assertEquals(removeTimezone(unixTime(2017, JULY, 30, 8, 0)), (unixTime(2017, JULY, 30, 18, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 29, 14, 0)), (unixTime(2017, SEPTEMBER, 30,  0, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30,  0, 0)), (unixTime(2017, SEPTEMBER, 30, 10, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30,  1, 0)), (unixTime(2017, SEPTEMBER, 30, 11, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30,  2, 0)), (unixTime(2017, SEPTEMBER, 30, 12, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30,  3, 0)), (unixTime(2017, SEPTEMBER, 30, 13, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 12, 0)), (unixTime(2017, SEPTEMBER, 30, 22, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 13, 0)), (unixTime(2017, SEPTEMBER, 30, 23, 0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 14,  0)), (unixTime(2017, OCTOBER, 1,  0,  0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 15,  0)), (unixTime(2017, OCTOBER, 1,  1,  0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 15, 59)), (unixTime(2017, OCTOBER, 1,  1, 59)));
        // DST begins
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 16,  0)), (unixTime(2017, OCTOBER, 1,  3,  0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 17,  0)), (unixTime(2017, OCTOBER, 1,  4,  0)));
        assertEquals(removeTimezone(unixTime(2017, SEPTEMBER, 30, 18,  0)), (unixTime(2017, OCTOBER, 1,  5,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 0, 0)), (unixTime(2017, OCTOBER, 1, 11,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 1, 0)), (unixTime(2017, OCTOBER, 1, 12,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 2, 0)), (unixTime(2017, OCTOBER, 1, 13,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 3, 0)), (unixTime(2017, OCTOBER, 1, 14,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 4, 0)), (unixTime(2017, OCTOBER, 1, 15,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 1, 8, 0)), (unixTime(2017, OCTOBER, 1, 19,  0)));
        assertEquals(removeTimezone(unixTime(2017, OCTOBER, 2, 8, 0)), (unixTime(2017, OCTOBER, 2, 19, 0)));
        assertEquals(removeTimezone(unixTime(2017, NOVEMBER, 30, 8, 0)), (unixTime(2017, NOVEMBER, 30, 19, 0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 30, 13,  0)), (unixTime(2018, MARCH, 31,  0,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31,  1,  0)), (unixTime(2018, MARCH, 31, 12,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31,  7,  0)), (unixTime(2018, MARCH, 31, 18,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 13,  0)), (unixTime(2018, APRIL,  1,  0,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 14,  0)), (unixTime(2018, APRIL,  1,  1,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 14, 59)), (unixTime(2018, APRIL,  1,  1, 59)));
        // DST ends
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 16,  0)), (unixTime(2018, APRIL,  1,  2,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 17,  0)), (unixTime(2018, APRIL,  1,  3,  0)));
        assertEquals(removeTimezone(unixTime(2018, MARCH, 31, 18,  0)), (unixTime(2018, APRIL,  1,  4,  0)));
        assertEquals(removeTimezone(unixTime(2018, APRIL,  1,  0,  0)), (unixTime(2018, APRIL,  1, 10,  0)));
        assertEquals(removeTimezone(unixTime(2018, APRIL,  1,  8,  0)), (unixTime(2018, APRIL,  1, 18,  0)));
    }
}
