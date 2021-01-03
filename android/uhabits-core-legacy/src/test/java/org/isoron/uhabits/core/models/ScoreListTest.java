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

    private Timestamp today;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        today = DateUtils.getToday();
        habit = fixtures.createEmptyHabit();
    }

    @Test
    public void test_getValue()
    {
        check(0, 20);

        double[] expectedValues = {
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
        assertThat(habit.getScores().get(today).getValue(), closeTo(2/3.0, E));

        // Missing 2 repetitions out of 4 per week, the score should converge to 50%
        habit.setFrequency(new Frequency(4, 7));
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.5, E));
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
        assertThat(habit.getScores().get(today).getValue(), closeTo(1.0, 1e-3));
    }

    @Test
    public void shouldAchieveHighScoreInReasonableTime()
    {
        // Daily habits should achieve at least 99% in 3 months
        habit = fixtures.createEmptyHabit();
        habit.setFrequency(Frequency.DAILY);
        for (int i = 0; i < 90; i++) check(i);
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), greaterThan(0.99));

        // Weekly habits should achieve at least 99% in 9 months
        habit = fixtures.createEmptyHabit();
        habit.setFrequency(Frequency.WEEKLY);
        for (int i = 0; i < 39; i++) check(7 * i);
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), greaterThan(0.99));

        // Monthly habits should achieve at least 99% in 18 months
        habit.setFrequency(new Frequency(1, 30));
        for (int i = 0; i < 18; i++) check(30 * i);
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), greaterThan(0.99));
    }

    @Test
    public void test_recompute()
    {
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.0, E));

        check(0, 2);
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.101149, E));

        habit.setFrequency(new Frequency(1, 2));
        habit.recompute();

        assertThat(habit.getScores().get(today).getValue(), closeTo(0.054816, E));
    }

    @Test
    public void test_addThenRemove()
    {
        Habit habit = fixtures.createEmptyHabit();
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.0, E));

        habit.getOriginalEntries().add(new Entry(today, YES_MANUAL));
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.051922, E));

        habit.getOriginalEntries().add(new Entry(today, UNKNOWN));
        habit.recompute();
        assertThat(habit.getScores().get(today).getValue(), closeTo(0.0, E));
    }

    private void check(final int offset)
    {
        EntryList entries = habit.getOriginalEntries();
        entries.add(new Entry(today.minus(offset), YES_MANUAL));
    }

    private void check(final int from, final int to)
    {
        EntryList entries = habit.getOriginalEntries();
        for (int i = from; i < to; i++)
            entries.add(new Entry(today.minus(i), YES_MANUAL));
        habit.recompute();
    }

    private void check(ArrayList<Integer> values)
    {
        EntryList entries = habit.getOriginalEntries();
        for (int i = 0; i < values.size(); i++)
            if (values.get(i) == YES_MANUAL)
                entries.add(new Entry(today.minus(i), YES_MANUAL));
        habit.recompute();
    }

    private void addSkip(final int day)
    {
        EntryList entries = habit.getOriginalEntries();
        entries.add(new Entry(today.minus(day), Entry.SKIP));
    }

    private void checkScoreValues(double[] expectedValues)
    {
        Timestamp current = today;
        ScoreList scores = habit.getScores();
        for (double expectedValue : expectedValues)
        {
            assertThat(scores.get(current).getValue(), closeTo(expectedValue, E));
            current = current.minus(1);
        }
    }
}
