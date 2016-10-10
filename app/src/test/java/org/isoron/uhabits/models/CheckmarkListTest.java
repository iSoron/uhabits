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

package org.isoron.uhabits.models;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.io.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.models.Checkmark.*;

public class CheckmarkListTest extends BaseUnitTest
{
    // 8:00am, January 25th, 2015 (UTC)
    private long fixed_local_time = 1422172800000L;

    private Habit nonDailyHabit;

    private Habit emptyHabit;

    @Override
    public void setUp()
    {
        super.setUp();

        DateUtils.setFixedLocalTime(fixed_local_time);

        fixtures.createShortHabit();
        nonDailyHabit = fixtures.createShortHabit();
        habitList.add(nonDailyHabit);

        emptyHabit = fixtures.createEmptyHabit();
        habitList.add(emptyHabit);
    }

    @Test
    public void test_getAllValues_moveBackwardsInTime()
    {
        travelInTime(-3);

        int[] expectedValues = {
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }


    @Test
    public void test_getAllValues_moveForwardInTime()
    {
        travelInTime(3);

        int[] expectedValues = {
            UNCHECKED,
            UNCHECKED,
            UNCHECKED,
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_withEmptyHabit()
    {
        int[] expectedValues = new int[0];
        int[] actualValues = emptyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_withNonDailyHabit()
    {
        int[] expectedValues = {
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getTodayValue()
    {
        travelInTime(-1);
        assertThat(nonDailyHabit.getCheckmarks().getTodayValue(),
            equalTo(UNCHECKED));

        travelInTime(0);
        assertThat(nonDailyHabit.getCheckmarks().getTodayValue(),
            equalTo(CHECKED_EXPLICITLY));

        travelInTime(1);
        assertThat(nonDailyHabit.getCheckmarks().getTodayValue(),
            equalTo(UNCHECKED));
    }

    @Test
    public void test_getValues_withInvalidInterval()
    {
        int values[] = nonDailyHabit.getCheckmarks().getValues(100L, -100L);
        assertThat(values, equalTo(new int[0]));
    }

    @Test
    public void test_getValues_withValidInterval()
    {
        long from =
            DateUtils.getStartOfToday() - 15 * DateUtils.millisecondsInOneDay;
        long to =
            DateUtils.getStartOfToday() - 5 * DateUtils.millisecondsInOneDay;

        int[] expectedValues = {
            CHECKED_EXPLICITLY,
            UNCHECKED,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            UNCHECKED,
            UNCHECKED,
            UNCHECKED,
            UNCHECKED,
            UNCHECKED,
            UNCHECKED
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getValues(from, to);

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        String expectedCSV = "2015-01-25,2\n" +
                             "2015-01-24,0\n" +
                             "2015-01-23,1\n" +
                             "2015-01-22,2\n" +
                             "2015-01-21,2\n" +
                             "2015-01-20,2\n" +
                             "2015-01-19,0\n" +
                             "2015-01-18,1\n" +
                             "2015-01-17,2\n" +
                             "2015-01-16,2\n";


        StringWriter writer = new StringWriter();
        nonDailyHabit.getCheckmarks().writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private void travelInTime(int days)
    {
        DateUtils.setFixedLocalTime(
            fixed_local_time + days * DateUtils.millisecondsInOneDay);
    }
}
