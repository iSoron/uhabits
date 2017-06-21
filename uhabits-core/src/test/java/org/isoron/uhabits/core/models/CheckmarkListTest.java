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

package org.isoron.uhabits.core.models;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Checkmark.CHECKED_EXPLICITLY;
import static org.isoron.uhabits.core.models.Checkmark.CHECKED_IMPLICITLY;
import static org.isoron.uhabits.core.models.Checkmark.UNCHECKED;

public class CheckmarkListTest extends BaseUnitTest
{
    private long dayLength;

    private long today;

    private Habit nonDailyHabit;

    private Habit emptyHabit;

    private Habit numericalHabit;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        dayLength = DateUtils.millisecondsInOneDay;
        today = DateUtils.getStartOfToday();

        nonDailyHabit = fixtures.createShortHabit();
        habitList.add(nonDailyHabit);

        emptyHabit = fixtures.createEmptyHabit();
        habitList.add(emptyHabit);

        numericalHabit = fixtures.createNumericalHabit();
        habitList.add(numericalHabit);
    }

    @Test
    public void test_buildCheckmarksFromIntervals_1() throws Exception
    {
        Repetition reps[] = new Repetition[]{
            new Repetition(day(10), CHECKED_EXPLICITLY),
            new Repetition(day(5), CHECKED_EXPLICITLY),
            new Repetition(day(2), CHECKED_EXPLICITLY),
            new Repetition(day(1), CHECKED_EXPLICITLY),
        };

        ArrayList<CheckmarkList.Interval> intervals = new ArrayList<>();
        intervals.add(new CheckmarkList.Interval(day(10), day(8), day(8)));
        intervals.add(new CheckmarkList.Interval(day(6), day(5), day(4)));
        intervals.add(new CheckmarkList.Interval(day(2), day(2), day(1)));

        List<Checkmark> expected = new ArrayList<>();
        expected.add(new Checkmark(day(0), UNCHECKED));
        expected.add(new Checkmark(day(1), CHECKED_EXPLICITLY));
        expected.add(new Checkmark(day(2), CHECKED_EXPLICITLY));
        expected.add(new Checkmark(day(3), UNCHECKED));
        expected.add(new Checkmark(day(4), CHECKED_IMPLICITLY));
        expected.add(new Checkmark(day(5), CHECKED_EXPLICITLY));
        expected.add(new Checkmark(day(6), CHECKED_IMPLICITLY));
        expected.add(new Checkmark(day(7), UNCHECKED));
        expected.add(new Checkmark(day(8), CHECKED_IMPLICITLY));
        expected.add(new Checkmark(day(9), CHECKED_IMPLICITLY));
        expected.add(new Checkmark(day(10), CHECKED_EXPLICITLY));

        List<Checkmark> actual =
            CheckmarkList.buildCheckmarksFromIntervals(reps, intervals);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildCheckmarksFromIntervals_2() throws Exception
    {
        Repetition reps[] = new Repetition[]{
            new Repetition(day(0), CHECKED_EXPLICITLY),
        };

        ArrayList<CheckmarkList.Interval> intervals = new ArrayList<>();
        intervals.add(new CheckmarkList.Interval(day(0), day(0), day(-10)));

        List<Checkmark> expected = new ArrayList<>();
        expected.add(new Checkmark(day(0), CHECKED_EXPLICITLY));

        List<Checkmark> actual =
            CheckmarkList.buildCheckmarksFromIntervals(reps, intervals);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildIntervals_1() throws Exception
    {
        Repetition reps[] = new Repetition[]{
            new Repetition(day(23), CHECKED_EXPLICITLY),
            new Repetition(day(18), CHECKED_EXPLICITLY),
            new Repetition(day(8), CHECKED_EXPLICITLY),
        };

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(23), day(23), day(17)));
        expected.add(new CheckmarkList.Interval(day(18), day(18), day(12)));
        expected.add(new CheckmarkList.Interval(day(8), day(8), day(2)));

        ArrayList<CheckmarkList.Interval> actual;
        actual = CheckmarkList.buildIntervals(Frequency.WEEKLY, reps);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildIntervals_2() throws Exception
    {
        Repetition reps[] = new Repetition[]{
            new Repetition(day(23), CHECKED_EXPLICITLY),
            new Repetition(day(18), CHECKED_EXPLICITLY),
            new Repetition(day(8), CHECKED_EXPLICITLY),
        };

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(23), day(23), day(23)));
        expected.add(new CheckmarkList.Interval(day(18), day(18), day(18)));
        expected.add(new CheckmarkList.Interval(day(8), day(8), day(8)));

        ArrayList<CheckmarkList.Interval> actual;
        actual = CheckmarkList.buildIntervals(Frequency.DAILY, reps);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildIntervals_3() throws Exception
    {
        Repetition reps[] = new Repetition[]{
            new Repetition(day(23), CHECKED_EXPLICITLY),
            new Repetition(day(22), CHECKED_EXPLICITLY),
            new Repetition(day(18), CHECKED_EXPLICITLY),
            new Repetition(day(15), CHECKED_EXPLICITLY),
            new Repetition(day(8), CHECKED_EXPLICITLY),
        };

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(23), day(22), day(17)));
        expected.add(new CheckmarkList.Interval(day(22), day(18), day(16)));
        expected.add(new CheckmarkList.Interval(day(18), day(15), day(12)));

        ArrayList<CheckmarkList.Interval> actual;
        actual =
            CheckmarkList.buildIntervals(Frequency.TWO_TIMES_PER_WEEK, reps);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_getAllValues_moveBackwardsInTime()
    {
        travelInTime(-3);

        int[] expectedValues = {
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_IMPLICITLY,
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
            CHECKED_IMPLICITLY,
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
            CHECKED_IMPLICITLY,
            CHECKED_IMPLICITLY,
            CHECKED_EXPLICITLY,
            CHECKED_EXPLICITLY
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getByInterval_withNumericalHabits() throws Exception
    {
        CheckmarkList checkmarks = numericalHabit.getCheckmarks();

        List<Checkmark> expected =
            Arrays.asList(new Checkmark(day(1), 200), new Checkmark(day(2), 0),
                new Checkmark(day(3), 300), new Checkmark(day(4), 0),
                new Checkmark(day(5), 400));

        List<Checkmark> actual = checkmarks.getByInterval(day(5), day(1));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_getTodayValue()
    {
        CheckmarkList checkmarks = nonDailyHabit.getCheckmarks();

        travelInTime(-1);
        assertThat(checkmarks.getTodayValue(), equalTo(UNCHECKED));

        travelInTime(0);
        assertThat(checkmarks.getTodayValue(), equalTo(CHECKED_EXPLICITLY));

        travelInTime(1);
        assertThat(checkmarks.getTodayValue(), equalTo(UNCHECKED));
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
        long from = today - 15 * dayLength;
        long to = today - 5 * dayLength;

        int[] expectedValues = {
            CHECKED_EXPLICITLY,
            CHECKED_IMPLICITLY,
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
    public void test_snapIntervalsTogether_1() throws Exception
    {
        ArrayList<CheckmarkList.Interval> original = new ArrayList<>();
        original.add(new CheckmarkList.Interval(day(40), day(40), day(34)));
        original.add(new CheckmarkList.Interval(day(25), day(25), day(19)));
        original.add(new CheckmarkList.Interval(day(16), day(16), day(10)));
        original.add(new CheckmarkList.Interval(day(8), day(8), day(2)));

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(40), day(40), day(34)));
        expected.add(new CheckmarkList.Interval(day(25), day(25), day(19)));
        expected.add(new CheckmarkList.Interval(day(18), day(16), day(12)));
        expected.add(new CheckmarkList.Interval(day(11), day(8), day(5)));

        CheckmarkList.snapIntervalsTogether(original);
        assertThat(original, equalTo(expected));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        String expectedCSV = "2015-01-25,2\n2015-01-24,0\n2015-01-23,1\n" +
                             "2015-01-22,2\n2015-01-21,2\n2015-01-20,2\n" +
                             "2015-01-19,1\n2015-01-18,1\n2015-01-17,2\n" +
                             "2015-01-16,2\n";


        StringWriter writer = new StringWriter();
        nonDailyHabit.getCheckmarks().writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private long day(int offset)
    {
        return DateUtils.getStartOfToday() -
               offset * DateUtils.millisecondsInOneDay;
    }

    private void travelInTime(int days)
    {
        DateUtils.setFixedLocalTime(
            FIXED_LOCAL_TIME + days * DateUtils.millisecondsInOneDay);
    }
}
