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
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ScoreListTest extends BaseUnitTest
{
    private Habit habit;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();
    }

    @Test
    public void test_getAll()
    {
        toggleRepetitions(0, 20);

        int expectedValues[] = {
            12629351,
            12266245,
            11883254,
            11479288,
            11053198,
            10603773,
            10129735,
            9629735,
            9102352,
            8546087,
            7959357,
            7340494,
            6687738,
            5999234,
            5273023,
            4507040,
            3699107,
            2846927,
            1948077,
            1000000
        };

        int actualValues[] = new int[expectedValues.length];

        int i = 0;
        for (Score s : habit.getScores())
            actualValues[i++] = s.getValue();

        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void test_getTodayValue()
    {
        toggleRepetitions(0, 20);
        assertThat(habit.getScores().getTodayValue(), equalTo(12629351));
    }

    @Test
    public void test_getValue()
    {
        toggleRepetitions(0, 20);

        int expectedValues[] = {
            12629351,
            12266245,
            11883254,
            11479288,
            11053198,
            10603773,
            10129735,
            9629735,
            9102352,
            8546087,
            7959357,
            7340494,
            6687738,
            5999234,
            5273023,
            4507040,
            3699107,
            2846927,
            1948077,
            1000000,
            0,
            0,
            0
        };

        ScoreList scores = habit.getScores();
        long current = DateUtils.getStartOfToday();
        for (int expectedValue : expectedValues)
        {
            assertThat(scores.getValue(current), equalTo(expectedValue));
            current -= DateUtils.millisecondsInOneDay;
        }
    }

    @Test
    public void test_groupBy()
    {
        Habit habit = fixtures.createLongHabit();
        List<Score> list =
            habit.getScores().groupBy(DateUtils.TruncateField.MONTH);

        assertThat(list.size(), equalTo(5));
        assertThat(list.get(0).getValue(), equalTo(14634077));
        assertThat(list.get(1).getValue(), equalTo(12969133));
        assertThat(list.get(2).getValue(), equalTo(10595391));
    }

    @Test
    public void test_invalidateNewerThan()
    {
        assertThat(habit.getScores().getTodayValue(), equalTo(0));

        toggleRepetitions(0, 2);
        assertThat(habit.getScores().getTodayValue(), equalTo(1948077));

        habit.setFrequency(new Frequency(1, 2));
        habit.getScores().invalidateNewerThan(0);

        assertThat(habit.getScores().getTodayValue(), equalTo(1974654));
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        Habit habit = fixtures.createShortHabit();

        String expectedCSV = "2015-01-25,0.2649\n" +
                             "2015-01-24,0.2205\n" +
                             "2015-01-23,0.2283\n" +
                             "2015-01-22,0.2364\n" +
                             "2015-01-21,0.1909\n" +
                             "2015-01-20,0.1439\n" +
                             "2015-01-19,0.0952\n" +
                             "2015-01-18,0.0986\n" +
                             "2015-01-17,0.1021\n" +
                             "2015-01-16,0.0519\n";

        StringWriter writer = new StringWriter();
        habit.getScores().writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    @Test
    public void test_getValues()
    {
        toggleRepetitions(0, 20);

        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        long from = today - 4 * day;
        long to = today - 2 * day;

        int[] expected = {
                11883254,
                11479288,
                11053198,
        };

        int[] actual = habit.getScores().getValues(from, to);
        assertThat(actual, equalTo(expected));
    }

    private void toggleRepetitions(final int from, final int to)
    {
        RepetitionList reps = habit.getRepetitions();
        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        for (int i = from; i < to; i++)
            reps.toggleTimestamp(today - i * day);
    }
}
