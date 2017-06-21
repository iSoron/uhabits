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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.number.IsCloseTo.*;

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
        toggleRepetitions(0, 20);

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
        toggleRepetitions(0, 20);
        double actual = habit.getScores().getTodayValue();
        assertThat(actual, closeTo(0.655747, E));
    }

    @Test
    public void test_getValue()
    {
        toggleRepetitions(0, 20);

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

        ScoreList scores = habit.getScores();
        long current = DateUtils.getStartOfToday();
        for (double expectedValue : expectedValues)
        {
            assertThat(scores.getValue(current), closeTo(expectedValue, E));
            current -= DateUtils.millisecondsInOneDay;
        }
    }

    @Test
    public void test_getValues()
    {
        toggleRepetitions(0, 20);

        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        long from = today - 4 * day;
        long to = today - 2 * day;

        double[] expected = {
            0.617008, 0.596033, 0.573909,
        };

        double[] actual = habit.getScores().getValues(from, to);
        assertThat(actual.length, equalTo(expected.length));

        for (int i = 0; i < actual.length; i++)
            assertThat(actual[i], closeTo(expected[i], E));
    }

    @Test
    public void test_groupBy()
    {
        Habit habit = fixtures.createLongHabit();
        List<Score> list =
            habit.getScores().groupBy(DateUtils.TruncateField.MONTH);

        assertThat(list.size(), equalTo(5));
        assertThat(list.get(0).getValue(), closeTo(0.653659, E));
        assertThat(list.get(1).getValue(), closeTo(0.622715, E));
        assertThat(list.get(2).getValue(), closeTo(0.520997, E));
    }

    @Test
    public void test_invalidateNewerThan()
    {
        assertThat(habit.getScores().getTodayValue(), closeTo(0.0, E));

        toggleRepetitions(0, 2);
        assertThat(habit.getScores().getTodayValue(), closeTo(0.101149, E));

        habit.setFrequency(new Frequency(1, 2));
        habit.getScores().invalidateNewerThan(0);

        assertThat(habit.getScores().getTodayValue(), closeTo(0.051922, E));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        Habit habit = fixtures.createShortHabit();

        String expectedCSV = "2015-01-25,0.2654\n2015-01-24,0.2389\n" +
                             "2015-01-23,0.2475\n2015-01-22,0.2203\n" +
                             "2015-01-21,0.1921\n2015-01-20,0.1628\n" +
                             "2015-01-19,0.1325\n2015-01-18,0.1011\n" +
                             "2015-01-17,0.0686\n2015-01-16,0.0349\n";

        StringWriter writer = new StringWriter();
        habit.getScores().writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private void toggleRepetitions(final int from, final int to)
    {
        RepetitionList reps = habit.getRepetitions();
        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        for (int i = from; i < to; i++)
            reps.toggle(today - i * day);
    }
}
