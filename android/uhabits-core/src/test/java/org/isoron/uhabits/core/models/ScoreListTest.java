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
import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.isoron.uhabits.core.models.Entry.*;

public class ScoreListTest extends BaseUnitTest
{
    private static final double E = 1e-6;

    private Habit habit;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();
    }

    @Test
    public void test_getAll()
    {
        check(0, 20);

        double expectedValues[] = {
            0.655747,
            0.636894,
            0.617008,
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067,
            0.147820,
            0.101149,
            0.051922,
        };

        int i = 0;
        for (Score s : habit.getScores())
            assertThat(s.getValue(), closeTo(expectedValues[i++], E));
    }

    @Test
    public void test_getTodayValue()
    {
        check(0, 20);
        double actual = habit.getScores().getTodayValue();
        assertThat(actual, closeTo(0.655747, E));
    }

    @Test
    public void test_getValue()
    {
        check(0, 20);

        double expectedValues[] = {
            0.655747,
            0.636894,
            0.617008,
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067,
            0.147820,
            0.101149,
            0.051922,
            0.000000,
            0.000000,
            0.000000
        };

        checkScoreValues(expectedValues);
    }

    @Test
    public void test_getValueWithSkip()
    {
        check(0, 20);
        addSkip(5);
        addSkip(10);
        addSkip(11);
        habit.recompute();

        double expectedValues[] = {
                0.596033,
                0.573910,
                0.550574,
                0.525961,
                0.500000,
                0.472617,
                0.472617,
                0.443734,
                0.413270,
                0.381137,
                0.347244,
                0.347244,
                0.347244,
                0.311495,
                0.273788,
                0.234017,
                0.192067,
                0.147820,
                0.101149,
                0.051922,
                0.000000,
                0.000000,
                0.000000
        };

        checkScoreValues(expectedValues);
    }

    @Test
    public void test_getValueWithSkip2()
    {
        check(5);
        addSkip(4);
        habit.recompute();

        double[] expectedValues = {
                0.041949,
                0.044247,
                0.046670,
                0.049226,
                0.051922,
                0.051922,
                0.0
        };

        checkScoreValues(expectedValues);
    }

    @Test
    public void test_getValues()
    {
        check(0, 20);

        Timestamp today = DateUtils.getToday();
        Timestamp from = today.minus(4);
        Timestamp to = today.minus(2);

        double[] expected = {
            0.617008, 0.596033, 0.573909,
        };

        double[] actual = habit.getScores().getValues(from, to);
        assertThat(actual.length, equalTo(expected.length));

        for (int i = 0; i < actual.length; i++)
            assertThat(actual[i], closeTo(expected[i], E));
    }

    @Test
    public void test_imperfectNonDaily()
    {
        // If the habit should be performed 3 times per week and the user misses 1 repetition
        // each week, score should converge to 66%.
        habit.setFrequency(new Frequency(3, 7));
        ArrayList<Integer> values = new ArrayList<>();
        for (int k = 0; k < 100; k++)
        {
            values.add(YES_MANUAL);
            values.add(YES_MANUAL);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
        }
        check(values);
        assertThat(habit.getScores().getTodayValue(), closeTo(2/3.0, E));

        // Missing 2 repetitions out of 4 per week, the score should converge to 50%
        habit.setFrequency(new Frequency(4, 7));
        habit.recompute();
        assertThat(habit.getScores().getTodayValue(), closeTo(0.5, E));
    }

    @Test
    public void test_irregularNonDaily()
    {
        // If the user performs habit perfectly each week, but on different weekdays,
        // score should still converge to 100%
        habit.setFrequency(new Frequency(1, 7));
        ArrayList<Integer> values = new ArrayList<>();
        for (int k = 0; k < 100; k++)
        {
            // Week 0
            values.add(YES_MANUAL);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);

            // Week 1
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(NO);
            values.add(YES_MANUAL);
        }
        check(values);
        assertThat(habit.getScores().getTodayValue(), closeTo(1.0, 1e-3));
    }

    @Test
    public void shouldAchieveHighScoreInReasonableTime()
    {
        // Daily habits should achieve at least 99% in 3 months
        habit = fixtures.createEmptyHabit();
        habit.setFrequency(Frequency.DAILY);
        for (int i = 0; i < 90; i++) check(i);
        habit.recompute();
        assertThat(habit.getScores().getTodayValue(), greaterThan(0.99));

        // Weekly habits should achieve at least 99% in 9 months
        habit = fixtures.createEmptyHabit();
        habit.setFrequency(Frequency.WEEKLY);
        for (int i = 0; i < 39; i++) check(7 * i);
        habit.recompute();
        assertThat(habit.getScores().getTodayValue(), greaterThan(0.99));

        // Monthly habits should achieve at least 99% in 18 months
        habit.setFrequency(new Frequency(1, 30));
        for (int i = 0; i < 18; i++) check(30 * i);
        habit.recompute();
        assertThat(habit.getScores().getTodayValue(), greaterThan(0.99));
    }

    @Test
    public void test_groupBy()
    {
        Habit habit = fixtures.createLongHabit();
        List<Score> list =
            habit.getScores().groupBy(DateUtils.TruncateField.MONTH, Calendar.SATURDAY);

        assertThat(list.size(), equalTo(5));
        assertThat(list.get(0).getValue(), closeTo(0.644120, E));
        assertThat(list.get(1).getValue(), closeTo(0.713651, E));
        assertThat(list.get(2).getValue(), closeTo(0.571922, E));
    }

    @Test
    public void test_recompute()
    {
        assertThat(habit.getScores().getTodayValue(), closeTo(0.0, E));

        check(0, 2);
        assertThat(habit.getScores().getTodayValue(), closeTo(0.101149, E));

        habit.setFrequency(new Frequency(1, 2));
        habit.getScores().recompute();

        assertThat(habit.getScores().getTodayValue(), closeTo(0.054816, E));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        Habit habit = fixtures.createShortHabit();

        String expectedCSV =
                "2015-01-25,0.2557\n" +
                "2015-01-24,0.2226\n" +
                "2015-01-23,0.1991\n" +
                "2015-01-22,0.1746\n" +
                "2015-01-21,0.1379\n" +
                "2015-01-20,0.0995\n" +
                "2015-01-19,0.0706\n" +
                "2015-01-18,0.0515\n" +
                "2015-01-17,0.0315\n" +
                "2015-01-16,0.0107\n";

        StringWriter writer = new StringWriter();
        habit.getScores().writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private void check(final int offset)
    {
        EntryList entries = habit.getOriginalEntries();
        Timestamp today = DateUtils.getToday();
        entries.add(new Entry(today.minus(offset), YES_MANUAL));
    }

    private void check(final int from, final int to)
    {
        EntryList entries = habit.getOriginalEntries();
        Timestamp today = DateUtils.getToday();

        for (int i = from; i < to; i++)
            entries.add(new Entry(today.minus(i), YES_MANUAL));
        habit.recompute();
    }

    private void check(ArrayList<Integer> values)
    {
        EntryList entries = habit.getOriginalEntries();
        Timestamp today = DateUtils.getToday();
        for (int i = 0; i < values.size(); i++)
            if (values.get(i) == YES_MANUAL)
                entries.add(new Entry(today.minus(i), YES_MANUAL));
        habit.recompute();
    }

    private void addSkip(final int day)
    {
        EntryList entries = habit.getOriginalEntries();
        Timestamp today = DateUtils.getToday();
        entries.add(new Entry(today.minus(day), Entry.SKIP));
    }

    private void checkScoreValues(double[] expectedValues)
    {
        Timestamp current = DateUtils.getToday();
        ScoreList scores = habit.getScores();
        for (double expectedValue : expectedValues)
        {
            assertThat(scores.getValue(current), closeTo(expectedValue, E));
            current = current.minus(1);
        }
    }
}
