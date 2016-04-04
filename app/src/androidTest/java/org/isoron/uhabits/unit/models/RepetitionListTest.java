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

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Repetition;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RepetitionListTest extends BaseTest
{
    private Habit habit;
    private Habit emptyHabit;

    @Before
    public void setup()
    {
        super.setup();

        HabitFixtures.purgeHabits();
        habit = HabitFixtures.createShortHabit();
        emptyHabit = HabitFixtures.createEmptyHabit();
    }

    @After
    public void tearDown()
    {
        DateHelper.setFixedLocalTime(null);
    }

    @Test
    public void test_contains()
    {
        long current = DateHelper.getStartOfToday();

        for(boolean b : HabitFixtures.NON_DAILY_HABIT_CHECKS)
        {
            assertThat(habit.repetitions.contains(current), equalTo(b));
            current -= DateHelper.millisecondsInOneDay;
        }

        for(int i = 0; i < 3; i++)
        {
            assertThat(habit.repetitions.contains(current), equalTo(false));
            current -= DateHelper.millisecondsInOneDay;
        }
    }

    @Test
    public void test_delete()
    {
        long timestamp = DateHelper.getStartOfToday();
        assertThat(habit.repetitions.contains(timestamp), equalTo(true));

        habit.repetitions.delete(timestamp);
        assertThat(habit.repetitions.contains(timestamp), equalTo(false));
    }

    @Test
    public void test_toggle()
    {
        long timestamp = DateHelper.getStartOfToday();
        assertThat(habit.repetitions.contains(timestamp), equalTo(true));

        habit.repetitions.toggle(timestamp);
        assertThat(habit.repetitions.contains(timestamp), equalTo(false));

        habit.repetitions.toggle(timestamp);
        assertThat(habit.repetitions.contains(timestamp), equalTo(true));
    }

    @Test
    public void test_getWeekDayFrequency()
    {
        Random random = new Random();
        Integer weekdayCount[][] = new Integer[12][7];
        Integer monthCount[] = new Integer[12];

        Arrays.fill(monthCount, 0);
        for(Integer row[] : weekdayCount)
            Arrays.fill(row, 0);

        GregorianCalendar day = DateHelper.getStartOfTodayCalendar();

        // Sets the current date to the end of November
        day.set(2015, 10, 30);
        DateHelper.setFixedLocalTime(day.getTimeInMillis());

        // Add repetitions randomly from January to December
        // Leaves the month of March empty, to check that it returns null
        day.set(2015, 0, 1);
        for(int i = 0; i < 365; i ++)
        {
            if(random.nextBoolean())
            {
                int month = day.get(Calendar.MONTH);
                int week = day.get(Calendar.DAY_OF_WEEK) % 7;

                if(month != 2)
                {
                    if (month <= 10)
                    {
                        weekdayCount[month][week]++;
                        monthCount[month]++;
                    }
                    emptyHabit.repetitions.toggle(day.getTimeInMillis());
                }
            }

            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        HashMap<Long, Integer[]> freq = emptyHabit.repetitions.getWeekdayFrequency();

        // Repetitions until November should be counted correctly
        for(int month = 0; month < 11; month++)
        {
            day.set(2015, month, 1);
            Integer actualCount[] = freq.get(day.getTimeInMillis());
            if(monthCount[month] == 0)
                assertThat(actualCount, equalTo(null));
            else
                assertThat(actualCount, equalTo(weekdayCount[month]));
        }

        // Repetitions in December should be discarded
        day.set(2015, 11, 1);
        assertThat(freq.get(day.getTimeInMillis()), equalTo(null));
    }

    @Test
    public void test_count()
    {
        long to = DateHelper.getStartOfToday();
        long from = to - 9 * DateHelper.millisecondsInOneDay;
        assertThat(habit.repetitions.count(from, to), equalTo(6));

        to = DateHelper.getStartOfToday() - DateHelper.millisecondsInOneDay;
        from = to - 5 * DateHelper.millisecondsInOneDay;
        assertThat(habit.repetitions.count(from, to), equalTo(3));
    }

    @Test
    public void test_getOldest()
    {
        long expectedOldestTimestamp = DateHelper.getStartOfToday() - 9 * DateHelper.millisecondsInOneDay;

        assertThat(habit.repetitions.getOldestTimestamp(), equalTo(expectedOldestTimestamp));

        Repetition oldest = habit.repetitions.getOldest();
        assertFalse(oldest == null);
        assertThat(oldest.timestamp, equalTo(expectedOldestTimestamp));
    }
}
