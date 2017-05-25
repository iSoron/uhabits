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

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.mockito.Mockito.*;

public class RepetitionListTest extends BaseUnitTest
{
    @NonNull
    private RepetitionList reps;

    @NonNull
    private Habit habit;

    private long today;

    private long day;

    @NonNull
    private ModelObservable.Listener listener;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();
        reps = habit.getRepetitions();

        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;

        reps.toggleTimestamp(today - 3 * day);
        reps.toggleTimestamp(today - 2 * day);
        reps.toggleTimestamp(today);
        reps.toggleTimestamp(today - 7 * day);
        reps.toggleTimestamp(today - 5 * day);

        listener = mock(ModelObservable.Listener.class);
        reps.getObservable().addListener(listener);
    }

    @Override
    @After
    public void tearDown()
    {
        super.tearDown();
    }

    @Test
    public void test_contains()
    {
        assertThat(reps.containsTimestamp(today), is(true));
        assertThat(reps.containsTimestamp(today - 2 * day), is(true));
        assertThat(reps.containsTimestamp(today - 3 * day), is(true));

        assertThat(reps.containsTimestamp(today - day), is(false));
        assertThat(reps.containsTimestamp(today - 4 * day), is(false));
    }

    @Test
    public void test_getOldest()
    {
        Repetition rep = reps.getOldest();
        assertThat(rep.getTimestamp(), is(equalTo(today - 7 * day)));
    }

    @Test
    public void test_getWeekDayFrequency()
    {
        habit = fixtures.createEmptyHabit();
        reps = habit.getRepetitions();

        Random random = new Random();
        Integer weekdayCount[][] = new Integer[12][7];
        Integer monthCount[] = new Integer[12];

        Arrays.fill(monthCount, 0);
        for (Integer row[] : weekdayCount)
            Arrays.fill(row, 0);

        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        // Sets the current date to the end of November
        day.set(2015, 10, 30);
        DateUtils.setFixedLocalTime(day.getTimeInMillis());

        // Add repetitions randomly from January to December
        // Leaves the month of March empty, to check that it returns null
        day.set(2015, 0, 1);
        for (int i = 0; i < 365; i++)
        {
            if (random.nextBoolean())
            {
                int month = day.get(Calendar.MONTH);
                int week = day.get(Calendar.DAY_OF_WEEK) % 7;

                if (month != 2)
                {
                    if (month <= 10)
                    {
                        weekdayCount[month][week]++;
                        monthCount[month]++;
                    }
                    reps.toggleTimestamp(day.getTimeInMillis());
                }
            }

            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        HashMap<Long, Integer[]> freq =
            reps.getWeekdayFrequency();

        // Repetitions until November should be counted correctly
        for (int month = 0; month < 11; month++)
        {
            day.set(2015, month, 1);
            Integer actualCount[] = freq.get(day.getTimeInMillis());
            if (monthCount[month] == 0) assertThat(actualCount, equalTo(null));
            else assertThat(actualCount, equalTo(weekdayCount[month]));
        }

        // Repetitions in December should be discarded
        day.set(2015, 11, 1);
        assertThat(freq.get(day.getTimeInMillis()), equalTo(null));
    }

    @Test
    public void test_toggleTimestamp()
    {
        assertThat(reps.containsTimestamp(today), equalTo(true));
        reps.toggleTimestamp(today);
        assertThat(reps.containsTimestamp(today), equalTo(false));
        verify(listener).onModelChange();
        reset(listener);

        assertThat(reps.containsTimestamp(today - day), equalTo(false));
        reps.toggleTimestamp(today - day);
        assertThat(reps.containsTimestamp(today - day), equalTo(true));
        verify(listener).onModelChange();
    }
}