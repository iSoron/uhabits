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

import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ScoreListTest
{
    private Habit habit;

    @Before
    public void prepare()
    {
        HabitFixtures.purgeHabits();
        DateHelper.setFixedLocalTime(HabitFixtures.FIXED_LOCAL_TIME);
        habit = HabitFixtures.createEmptyHabit();
    }

    @After
    public void tearDown()
    {
        DateHelper.setFixedLocalTime(null);
    }

    @Test
    public void invalidateNewerThan()
    {
        assertThat(habit.scores.getTodayValue(), equalTo(0));

        toggleRepetitions(0, 2);
        assertThat(habit.scores.getTodayValue(), equalTo(1948077));

        habit.freqNum = 1;
        habit.freqDen = 2;
        habit.scores.invalidateNewerThan(0);

        assertThat(habit.scores.getTodayValue(), equalTo(1974654));
    }

    @Test
    public void getTodayStarValue()
    {
        assertThat(habit.scores.getTodayStarStatus(), equalTo(Score.EMPTY_STAR));

        int k = 0;
        while(habit.scores.getTodayValue() < Score.HALF_STAR_CUTOFF) toggleRepetitions(k, ++k);
        assertThat(habit.scores.getTodayStarStatus(), equalTo(Score.HALF_STAR));

        while(habit.scores.getTodayValue() < Score.FULL_STAR_CUTOFF) toggleRepetitions(k, ++k);
        assertThat(habit.scores.getTodayStarStatus(), equalTo(Score.FULL_STAR));
    }

    @Test
    public void getTodayValue()
    {
        toggleRepetitions(0, 20);
        assertThat(habit.scores.getTodayValue(), equalTo(12629351));
    }

    @Test
    public void getValue()
    {
        toggleRepetitions(0, 20);

        int expectedValues[] = { 12629351, 12266245, 11883254, 11479288, 11053198, 10603773,
                10129735, 9629735, 9102352, 8546087, 7959357, 7340494, 6687738, 5999234, 5273023,
                4507040, 3699107, 2846927, 1948077, 1000000 };

        long current = DateHelper.getStartOfToday();
        for(int expectedValue : expectedValues)
        {
            assertThat(habit.scores.getValue(current), equalTo(expectedValue));
            current -= DateHelper.millisecondsInOneDay;
        }
    }

    @Test
    public void getAllValues_withoutGroups()
    {
        toggleRepetitions(0, 20);

        int expectedValues[] = { 12629351, 12266245, 11883254, 11479288, 11053198, 10603773,
                10129735, 9629735, 9102352, 8546087, 7959357, 7340494, 6687738, 5999234, 5273023,
                4507040, 3699107, 2846927, 1948077, 1000000 };

        int actualValues[] = habit.scores.getAllValues(1);
        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void getAllValues_withGroups()
    {
        toggleRepetitions(0, 20);

        int expectedValues[] = { 11434978, 7894999, 3212362 };

        int actualValues[] = habit.scores.getAllValues(7);
        assertThat(actualValues, equalTo(expectedValues));
    }

    @Test
    public void writeCSV() throws IOException
    {
        HabitFixtures.purgeHabits();
        Habit habit = HabitFixtures.createNonDailyHabit();

        String expectedCSV =
                "2015-01-16,0.0519\n" +
                "2015-01-17,0.1021\n" +
                "2015-01-18,0.0986\n" +
                "2015-01-19,0.0952\n" +
                "2015-01-20,0.1439\n" +
                "2015-01-21,0.1909\n" +
                "2015-01-22,0.2364\n" +
                "2015-01-23,0.2283\n" +
                "2015-01-24,0.2205\n" +
                "2015-01-25,0.2649\n";

        StringWriter writer = new StringWriter();
        habit.scores.writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    private void toggleRepetitions(final int from, final int to)
    {
        DatabaseHelper.executeAsTransaction(new DatabaseHelper.Command()
        {
            @Override
            public void execute()
            {
                long today = DateHelper.getStartOfToday();
                for (int i = from; i < to; i++)
                    habit.repetitions.toggle(today - i * DateHelper.millisecondsInOneDay);
            }
        });
    }
}
