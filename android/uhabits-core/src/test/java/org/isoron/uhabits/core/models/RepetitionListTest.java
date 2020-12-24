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

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.util.*;

import static java.util.Calendar.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Entry.*;
import static org.mockito.Mockito.*;

public class RepetitionListTest extends BaseUnitTest
{
    @NonNull
    private RepetitionList reps;

    @NonNull
    private Habit habit;

    private Timestamp today;

    private long day;

    @NonNull
    private ModelObservable.Listener listener;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();
        reps = habit.getOriginalEntries();

        today = DateUtils.getToday();

        reps.setValue(today.minus(3), YES_MANUAL);
        reps.setValue(today.minus(2), YES_MANUAL);
        reps.setValue(today, YES_MANUAL);
        reps.setValue(today.minus(7), YES_MANUAL);
        reps.setValue(today.minus(5), YES_MANUAL);

        listener = mock(ModelObservable.Listener.class);
        reps.getObservable().addListener(listener);
        reset(listener);
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void test_getOldest()
    {
        Entry check = reps.getOldest();
        assertThat(check.getTimestamp(), equalTo(today.minus(7)));
    }

    @Test
    public void test_getWeekDayFrequency()
    {
        habit = fixtures.createEmptyHabit();
        reps = habit.getOriginalEntries();

        Random random = new Random(123L);
        Integer weekdayCount[][] = new Integer[12][7];
        Integer monthCount[] = new Integer[12];

        Arrays.fill(monthCount, 0);
        for (Integer row[] : weekdayCount) Arrays.fill(row, 0);
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        // Sets the current date to the end of November
        day.set(2015, NOVEMBER, 30, 12, 0, 0);
        DateUtils.setFixedLocalTime(day.getTimeInMillis());

        // Add repetitions randomly from January to December
        day.set(2015, JANUARY, 1, 0, 0, 0);
        for (int i = 0; i < 365; i++)
        {
            if (random.nextBoolean())
            {
                int month = day.get(Calendar.MONTH);
                int week = day.get(Calendar.DAY_OF_WEEK) % 7;

                // Leave the month of March empty, to check that it returns null
                if (month == MARCH) continue;

                reps.setValue(new Timestamp(day), YES_MANUAL);

                // Repetitions in December should not be counted
                if (month == DECEMBER) continue;

                weekdayCount[month][week]++;
                monthCount[month]++;
            }

            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        HashMap<Timestamp, Integer[]> freq = reps.getWeekdayFrequency();

        // Repetitions until November should be counted correctly
        for (int month = 0; month < 11; month++)
        {
            day.set(2015, month, 1, 0, 0, 0);
            Integer actualCount[] = freq.get(new Timestamp(day));
            if (monthCount[month] == 0) assertThat(actualCount, equalTo(null));
            else assertThat(actualCount, equalTo(weekdayCount[month]));
        }

        // Repetitions in December should be discarded
        day.set(2015, DECEMBER, 1, 0, 0, 0);
        assertThat(freq.get(new Timestamp(day)), equalTo(null));
    }

    @Test
    public void test_setValue()
    {
        assertThat(reps.getValue(today), equalTo(YES_MANUAL));
        reps.setValue(today, NO);
        assertThat(reps.getValue(today), equalTo(NO));
        verify(listener, times(2)).onModelChange();
        reset(listener);

        habit.setType(Habit.NUMBER_HABIT);
        reps.setValue(today, 100);
        assertThat(reps.getValue(today), equalTo(100));
        verify(listener, times(2)).onModelChange();
        reset(listener);

        reps.setValue(today, 500);
        assertThat(reps.getValue(today), equalTo(500));
        verify(listener, times(2)).onModelChange();
        reset(listener);
    }
}