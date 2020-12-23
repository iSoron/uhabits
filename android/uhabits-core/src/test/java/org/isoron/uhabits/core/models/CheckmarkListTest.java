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

import static java.util.Calendar.*;
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
                new Repetition(day(10), YES_MANUAL),
                new Repetition(day(5), YES_MANUAL),
                new Repetition(day(2), YES_MANUAL),
                new Repetition(day(1), YES_MANUAL),
                };

        ArrayList<CheckmarkList.Interval> intervals = new ArrayList<>();
        intervals.add(new CheckmarkList.Interval(day(10), day(8), day(8)));
        intervals.add(new CheckmarkList.Interval(day(6), day(5), day(4)));
        intervals.add(new CheckmarkList.Interval(day(2), day(2), day(1)));

        List<Checkmark> expected = new ArrayList<>();
        expected.add(new Checkmark(day(0), UNKNOWN));
        expected.add(new Checkmark(day(1), YES_MANUAL));
        expected.add(new Checkmark(day(2), YES_MANUAL));
        expected.add(new Checkmark(day(3), UNKNOWN));
        expected.add(new Checkmark(day(4), YES_AUTO));
        expected.add(new Checkmark(day(5), YES_MANUAL));
        expected.add(new Checkmark(day(6), YES_AUTO));
        expected.add(new Checkmark(day(7), UNKNOWN));
        expected.add(new Checkmark(day(8), YES_AUTO));
        expected.add(new Checkmark(day(9), YES_AUTO));
        expected.add(new Checkmark(day(10), YES_MANUAL));

        List<Checkmark> actual =
                CheckmarkList.buildCheckmarksFromIntervals(reps, intervals);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildCheckmarksFromIntervals_2() throws Exception
    {
        Repetition reps[] = new Repetition[]{
                new Repetition(day(0), YES_MANUAL),
                };

        ArrayList<CheckmarkList.Interval> intervals = new ArrayList<>();
        intervals.add(new CheckmarkList.Interval(day(0), day(0), day(-10)));

        List<Checkmark> expected = new ArrayList<>();
        expected.add(new Checkmark(day(0), YES_MANUAL));

        List<Checkmark> actual =
                CheckmarkList.buildCheckmarksFromIntervals(reps, intervals);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_buildIntervals_1() throws Exception
    {
        Repetition reps[] = new Repetition[]{
                new Repetition(day(23), YES_MANUAL),
                new Repetition(day(18), YES_MANUAL),
                new Repetition(day(8), YES_MANUAL),
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
                new Repetition(day(23), YES_MANUAL),
                new Repetition(day(18), YES_MANUAL),
                new Repetition(day(8), YES_MANUAL),
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
                new Repetition(day(23), YES_MANUAL),
                new Repetition(day(22), YES_MANUAL),
                new Repetition(day(18), YES_MANUAL),
                new Repetition(day(15), YES_MANUAL),
                new Repetition(day(8), YES_MANUAL),
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
    public void test_buildIntervals_4() throws Exception
    {
        Repetition[] reps = new Repetition[]{
                new Repetition(day(30), YES_MANUAL),
                new Repetition(day(20), SKIP),
                new Repetition(day(10), YES_MANUAL),
        };

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(30), day(30), day(28)));
        expected.add(new CheckmarkList.Interval(day(10), day(10), day(8)));

        ArrayList<CheckmarkList.Interval> actual;
        actual = CheckmarkList.buildIntervals(new Frequency(1, 3), reps);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_getAllValues_moveBackwardsInTime()
    {
        travelInTime(-3);

        int[] expectedValues = {
                YES_MANUAL,
                YES_MANUAL,
                YES_MANUAL,
                YES_AUTO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getAllValues();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getAllValues_moveForwardInTime()
    {
        travelInTime(3);

        int[] expectedValues = {
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                YES_MANUAL,
                NO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL,
                YES_MANUAL,
                YES_AUTO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL
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
                YES_MANUAL,
                NO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL,
                YES_MANUAL,
                YES_AUTO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL
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
        assertThat(checkmarks.getTodayValue(), equalTo(NO));

        travelInTime(0);
        assertThat(checkmarks.getTodayValue(), equalTo(YES_MANUAL));

        travelInTime(1);
        assertThat(checkmarks.getTodayValue(), equalTo(UNKNOWN));
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
                YES_MANUAL,
                YES_AUTO,
                YES_AUTO,
                YES_MANUAL,
                YES_MANUAL,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN,
                UNKNOWN
        };

        int[] actualValues = nonDailyHabit.getCheckmarks().getValues(from, to);
        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_snapIntervalsTogether_1() throws Exception
    {
        ArrayList<CheckmarkList.Interval> original = new ArrayList<>();
        original.add(new CheckmarkList.Interval(day(27), day(27), day(21)));
        original.add(new CheckmarkList.Interval(day(20), day(20), day(14)));
        original.add(new CheckmarkList.Interval(day(12), day(12), day(6)));
        original.add(new CheckmarkList.Interval(day(8), day(8), day(2)));

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(29), day(27), day(23)));
        expected.add(new CheckmarkList.Interval(day(22), day(20), day(16)));
        expected.add(new CheckmarkList.Interval(day(15), day(12), day(9)));
        expected.add(new CheckmarkList.Interval(day(8), day(8), day(2)));

        CheckmarkList.snapIntervalsTogether(original);
        assertThat(original, equalTo(expected));
    }

    @Test
    public void test_snapIntervalsTogether_2() throws Exception
    {
        ArrayList<CheckmarkList.Interval> original = new ArrayList<>();
        original.add(new CheckmarkList.Interval(day(11), day(8), day(5)));
        original.add(new CheckmarkList.Interval(day(6), day(4), day(0)));

        ArrayList<CheckmarkList.Interval> expected = new ArrayList<>();
        expected.add(new CheckmarkList.Interval(day(13), day(8), day(7)));
        expected.add(new CheckmarkList.Interval(day(6), day(4), day(0)));

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

        List<Checkmark> byMonth = checkmarks.groupBy(MONTH, Calendar.SATURDAY);
        assertThat(byMonth.size(), equalTo(25)); // from 2013-01-01 to 2015-01-01
        assertThat(byMonth.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byMonth.get(6), equalTo(new Checkmark(timestamp(2014, JULY, 1), 0)));
        assertThat(byMonth.get(12), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 1706)));
        assertThat(byMonth.get(18), equalTo(new Checkmark(timestamp(2013, JULY, 1), 1379)));

        List<Checkmark> byQuarter = checkmarks.groupBy(QUARTER, Calendar.SATURDAY);
        assertThat(byQuarter.size(), equalTo(9)); // from 2013-Q1 to 2015-Q1
        assertThat(byQuarter.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byQuarter.get(4), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 4964)));
        assertThat(byQuarter.get(8), equalTo(new Checkmark(timestamp(2013, JANUARY, 1), 4975)));

        List<Checkmark> byYear = checkmarks.groupBy(YEAR, Calendar.SATURDAY);
        assertThat(byYear.size(), equalTo(3)); // from 2013 to 2015
        assertThat(byYear.get(0), equalTo(new Checkmark(timestamp(2015, JANUARY, 1), 0)));
        assertThat(byYear.get(1), equalTo(new Checkmark(timestamp(2014, JANUARY, 1), 8227)));
        assertThat(byYear.get(2), equalTo(new Checkmark(timestamp(2013, JANUARY, 1), 16172)));
    }

    @Test
    public void testGetTodayValue() throws Exception
    {
        Habit habit = fixtures.createLongNumericalHabit(timestamp(2014, JUNE, 1));
        CheckmarkList checkmarks = habit.getCheckmarks();

        DateUtils.setFixedLocalTime(unixTime(2050, MAY, 1));
        assertThat(checkmarks.getTodayValue(), equalTo(0));
        assertThat(checkmarks.getThisWeekValue(SATURDAY), equalTo(0));
        assertThat(checkmarks.getThisMonthValue(), equalTo(0));
        assertThat(checkmarks.getThisQuarterValue(), equalTo(0));
        assertThat(checkmarks.getThisYearValue(), equalTo(0));

        DateUtils.setFixedLocalTime(unixTime(2014, JUNE, 6));
        assertThat(checkmarks.getTodayValue(), equalTo(0));
        assertThat(checkmarks.getThisWeekValue(SATURDAY), equalTo(230));
        assertThat(checkmarks.getThisWeekValue(SUNDAY), equalTo(230));
        assertThat(checkmarks.getThisWeekValue(MONDAY), equalTo(0));
        assertThat(checkmarks.getThisMonthValue(), equalTo(230));
        assertThat(checkmarks.getThisQuarterValue(), equalTo(3263));
        assertThat(checkmarks.getThisYearValue(), equalTo(8227));

        DateUtils.setFixedLocalTime(unixTime(2014, JUNE, 1));
        assertThat(checkmarks.getTodayValue(), equalTo(230));
        assertThat(checkmarks.getThisWeekValue(SATURDAY), equalTo(230));
        assertThat(checkmarks.getThisWeekValue(SUNDAY), equalTo(230));
        assertThat(checkmarks.getThisMonthValue(), equalTo(230));

        DateUtils.setFixedLocalTime(unixTime(2014, MAY, 16));
        assertThat(checkmarks.getTodayValue(), equalTo(0));
        assertThat(checkmarks.getThisWeekValue(SATURDAY), equalTo(419));
        assertThat(checkmarks.getThisWeekValue(THURSDAY), equalTo(134));
        assertThat(checkmarks.getThisMonthValue(), equalTo(1006));

        DateUtils.setFixedLocalTime(unixTime(2000, MAY, 1));
        assertThat(checkmarks.getTodayValue(), equalTo(UNKNOWN));
        assertThat(checkmarks.getThisWeekValue(SATURDAY), equalTo(0));
        assertThat(checkmarks.getThisMonthValue(), equalTo(0));
    }
}
