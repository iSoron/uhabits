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

package org.isoron.uhabits.unit.models;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.isoron.uhabits.models.Checkmark.CHECKED_EXPLICITLY;
import static org.isoron.uhabits.models.Checkmark.CHECKED_IMPLICITLY;
import static org.isoron.uhabits.models.Checkmark.UNCHECKED;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CheckmarkListTest extends BaseTest
{
    Habit nonDailyHabit;
    private Habit emptyHabit;

    @Before
    public void setup()
    {
        super.setup();

        HabitFixtures.purgeHabits();
        nonDailyHabit = HabitFixtures.createShortHabit();
        emptyHabit = HabitFixtures.createEmptyHabit();
    }

    @After
    public void tearDown()
    {
        DateUtils.setFixedLocalTime(null);
    }

    @Test
    public void test_getAllValues_withNonDailyHabit()
    {
        int[] expectedValues = { CHECKED_EXPLICITLY, UNCHECKED, CHECKED_IMPLICITLY,
                CHECKED_EXPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY, UNCHECKED,
                CHECKED_IMPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY };

        int[] actualValues = nonDailyHabit.checkmarks.getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_withEmptyHabit()
    {
        int[] expectedValues = new int[0];
        int[] actualValues = emptyHabit.checkmarks.getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_moveForwardInTime()
    {
        travelInTime(3);

        int[] expectedValues = { UNCHECKED, UNCHECKED, UNCHECKED, CHECKED_EXPLICITLY, UNCHECKED,
                CHECKED_IMPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY,
                UNCHECKED, CHECKED_IMPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY };

        int[] actualValues = nonDailyHabit.checkmarks.getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_moveBackwardsInTime()
    {
        travelInTime(-3);

        int[] expectedValues = { CHECKED_EXPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY,
                UNCHECKED, CHECKED_IMPLICITLY, CHECKED_EXPLICITLY, CHECKED_EXPLICITLY };

        int[] actualValues = nonDailyHabit.checkmarks.getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getValues_withInvalidInterval()
    {
        int values[] = nonDailyHabit.checkmarks.getValues(100L, -100L);
        assertThat(values, equalTo(new int[0]));
    }

    @Test
    public void test_getValues_withValidInterval()
    {
        long from = DateUtils.getStartOfToday() - 15 * DateUtils.millisecondsInOneDay;
        long to = DateUtils.getStartOfToday() - 5 * DateUtils.millisecondsInOneDay;

        int[] expectedValues = { CHECKED_EXPLICITLY, UNCHECKED, CHECKED_IMPLICITLY,
                CHECKED_EXPLICITLY, CHECKED_EXPLICITLY, UNCHECKED, UNCHECKED, UNCHECKED, UNCHECKED,
                UNCHECKED, UNCHECKED };

        int[] actualValues = nonDailyHabit.checkmarks.getValues(from, to);

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getTodayValue()
    {
        travelInTime(-1);
        assertThat(nonDailyHabit.checkmarks.getTodayValue(), equalTo(UNCHECKED));

        travelInTime(0);
        assertThat(nonDailyHabit.checkmarks.getTodayValue(), equalTo(CHECKED_EXPLICITLY));

        travelInTime(1);
        assertThat(nonDailyHabit.checkmarks.getTodayValue(), equalTo(UNCHECKED));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        String expectedCSV =
                "2015-01-16,2\n" +
                "2015-01-17,2\n" +
                "2015-01-18,1\n" +
                "2015-01-19,0\n" +
                "2015-01-20,2\n" +
                "2015-01-21,2\n" +
                "2015-01-22,2\n" +
                "2015-01-23,1\n" +
                "2015-01-24,0\n" +
                "2015-01-25,2\n";

        StringWriter writer = new StringWriter();
        nonDailyHabit.checkmarks.writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private void travelInTime(int days)
    {
        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME +
                days * DateUtils.millisecondsInOneDay);
    }
}
