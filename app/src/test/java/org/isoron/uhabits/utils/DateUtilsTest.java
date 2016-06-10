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

import org.isoron.uhabits.BaseUnitTest;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DateUtilsTest extends BaseUnitTest
{
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

        expected = timestamp(2016, Calendar.DECEMBER, 1);
        t0 = timestamp(2016, Calendar.DECEMBER, 1);
        t1 = timestamp(2016, Calendar.DECEMBER, 15);
        t2 = timestamp(2016, Calendar.DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_quarter()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.QUARTER;

        long expected = timestamp(2016, Calendar.JANUARY, 1);
        long t0 = timestamp(2016, Calendar.JANUARY, 20);
        long t1 = timestamp(2016, Calendar.FEBRUARY, 15);
        long t2 = timestamp(2016, Calendar.MARCH, 30);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2016, Calendar.APRIL, 1);
        t0 = timestamp(2016, Calendar.APRIL, 1);
        t1 = timestamp(2016, Calendar.MAY, 30);
        t2 = timestamp(2016, Calendar.JUNE, 20);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    @Test
    public void testTruncate_year()
    {
        DateUtils.TruncateField field = DateUtils.TruncateField.YEAR;

        long expected = timestamp(2016, Calendar.JANUARY, 1);
        long t0 = timestamp(2016, Calendar.JANUARY, 1);
        long t1 = timestamp(2016, Calendar.FEBRUARY, 25);
        long t2 = timestamp(2016, Calendar.DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));

        expected = timestamp(2017, Calendar.JANUARY, 1);
        t0 = timestamp(2017, Calendar.JANUARY, 1);
        t1 = timestamp(2017, Calendar.MAY, 30);
        t2 = timestamp(2017, Calendar.DECEMBER, 31);

        assertThat(DateUtils.truncate(field, t0), equalTo(expected));
        assertThat(DateUtils.truncate(field, t1), equalTo(expected));
        assertThat(DateUtils.truncate(field, t2), equalTo(expected));
    }

    private void log(long timestamp)
    {
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        log("%s", df.format(new Date(timestamp)));
    }

    public long timestamp(int year, int month, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }
}
