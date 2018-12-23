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

import nl.jqno.equalsverifier.*;

import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.JUNE;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Checkmark.*;
import static org.isoron.uhabits.core.utils.DateUtils.TruncateField.MONTH;
import static org.isoron.uhabits.core.utils.DateUtils.TruncateField.QUARTER;
import static org.isoron.uhabits.core.utils.DateUtils.TruncateField.YEAR;

public class CheckmarkListTest extends BaseUnitTest
{
    private long dayLength;

    private Timestamp today;

    private Habit nonDailyHabit;

    private Habit emptyHabit;

    private Habit numericalHabit;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        nonDailyHabit = fixtures.createShortHabit();
        habitList.add(nonDailyHabit);

        emptyHabit = fixtures.createEmptyHabit();
        habitList.add(emptyHabit);

        numericalHabit = fixtures.createNumericalHabit();
        habitList.add(numericalHabit);
        today = DateUtils.getToday();
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
        int values[] = nonDailyHabit
            .getCheckmarks()
            .getValues(new Timestamp(0L).plus(100), new Timestamp(0L));
        assertThat(values, equalTo(new int[0]));
    }

    @Test
    public void test_getValues_withValidInterval()
    {
        Timestamp from = today.minus(15);
        Timestamp to = today.minus(5);

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

    private Timestamp day(int offset)
    {
        return DateUtils.getToday().minus(offset);
    }

    private void travelInTime(int days)
    {
        DateUtils.setFixedLocalTime(
            FIXED_LOCAL_TIME + days * Timestamp.DAY_LENGTH);
    }

    @Test
    public void testToString() throws Exception
    {
        Timestamp t = Timestamp.ZERO.plus(100);
        Checkmark checkmark = new Checkmark(t, 2);
        assertThat(checkmark.toString(),
            equalTo("{timestamp: 1970-04-11, value: 2}"));

        CheckmarkList.Interval interval =
            new CheckmarkList.Interval(t, t.plus(1), t.plus(2));
        assertThat(interval.toString(), equalTo(
            "{begin: 1970-04-11, center: 1970-04-12, end: 1970-04-13}"));
    }

    @Test
    public void testEquals() throws Exception
    {
        EqualsVerifier.forClass(Checkmark.class).verify();
        EqualsVerifier.forClass(Timestamp.class).verify();
        EqualsVerifier.forClass(CheckmarkList.Interval.class).verify();
    }

    @Test
    public void testGroupBy() throws Exception
    {
        Habit habit = fixtures.createLongNumericalHabit(timestamp(2014, JUNE, 1));
        CheckmarkList checkmarks = habit.getCheckmarks();

        List<Checkmark> byMonth = checkmarks.groupBy(MONTH);
        assertThat(byMonth.size(), equalTo(25)); // from 2013-01-01 to 2015-01-01
        assertThat(byMonth.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byMonth.get(6), equalTo(new Checkmark(timestamp(2014, JULY, 1), 0)));
        assertThat(byMonth.get(12), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 1706)));
        assertThat(byMonth.get(18), equalTo(new Checkmark(timestamp(2013, JULY, 1), 1379)));

        List<Checkmark> byQuarter = checkmarks.groupBy(QUARTER);
        assertThat(byQuarter.size(), equalTo(9)); // from 2013-Q1 to 2015-Q1
        assertThat(byQuarter.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byQuarter.get(4), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 4964)));
        assertThat(byQuarter.get(8), equalTo(new Checkmark(timestamp(2013, JANUARY, 1), 4975)));

        List<Checkmark> byYear = checkmarks.groupBy(YEAR);
        assertThat(byYear.size(), equalTo(3)); // from 2013 to 2015
        assertThat(byYear.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byYear.get(1), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 8227)));
        assertThat(byYear.get(2), equalTo(new Checkmark(timestamp(2013, JANUARY, 1), 16172)));
    }
}
