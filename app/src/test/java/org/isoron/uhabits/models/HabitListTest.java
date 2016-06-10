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

import org.hamcrest.MatcherAssert;
import org.isoron.uhabits.BaseUnitTest;
import org.isoron.uhabits.utils.DateUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HabitListTest extends BaseUnitTest
{
    private HabitList list;

    private ArrayList<Habit> habits;

    @Override
    public void setUp()
    {
        super.setUp();

        list = modelFactory.buildHabitList();
        habits = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            Habit habit = new Habit();
            habit.setId((long) i);
            habits.add(habit);
            list.add(habit);

            if (i % 3 == 0)
            {
                habit.setReminderDays(DateUtils.ALL_WEEK_DAYS);
                habit.setReminderHour(8);
                habit.setReminderMin(30);
            }
        }

        habits.get(0).setArchived(1);
        habits.get(1).setArchived(1);
        habits.get(4).setArchived(1);
        habits.get(7).setArchived(1);
    }

    @Test
    public void test_count()
    {
        assertThat(list.count(), equalTo(6));
    }

    @Test
    public void test_countWithArchived()
    {
        assertThat(list.countWithArchived(), equalTo(10));
    }

    @Test
    public void test_getAll()
    {
        List<Habit> filteredList = list.getAll(false);

        assertThat(filteredList.size(), equalTo(6));
        assertThat(filteredList.contains(habits.get(2)), is(true));
        assertThat(filteredList.contains(habits.get(4)), is(false));

        filteredList = list.getAll(true);

        assertThat(filteredList.size(), equalTo(10));
        assertThat(filteredList.contains(habits.get(2)), is(true));
        assertThat(filteredList.contains(habits.get(4)), is(true));
    }

    @Test
    public void test_getByPosition()
    {
        assertThat(list.getByPosition(0), equalTo(habits.get(0)));
        assertThat(list.getByPosition(3), equalTo(habits.get(3)));
        assertThat(list.getByPosition(9), equalTo(habits.get(9)));
    }

    @Test
    public void test_getHabitsWithReminder()
    {
        List<Habit> filtered = list.getWithReminder();
        assertThat(filtered.size(), equalTo(4));
        assertThat(filtered.contains(habits.get(0)), equalTo(true));
        assertThat(filtered.contains(habits.get(1)), equalTo(false));
    }

    @Test
    public void test_get_withInvalidId()
    {
        assertThat(list.getById(100L), is(nullValue()));
    }

    @Test
    public void test_get_withValidId()
    {
        Habit habit1 = habits.get(0);
        Habit habit2 = list.getById(habit1.getId());
        assertThat(habit1, equalTo(habit2));
    }

    @Test
    public void test_reorder()
    {
        int operations[][] = {
            {5, 2}, {3, 7}, {4, 4}, {3, 2}
        };

        int expectedPosition[][] = {
            {0, 1, 3, 4, 5, 2, 6, 7, 8, 9},
            {0, 1, 7, 3, 4, 2, 5, 6, 8, 9},
            {0, 1, 7, 3, 4, 2, 5, 6, 8, 9},
            {0, 1, 7, 2, 4, 3, 5, 6, 8, 9},
        };

        for (int i = 0; i < operations.length; i++)
        {
            int from = operations[i][0];
            int to = operations[i][1];

            Habit fromHabit = list.getByPosition(from);
            Habit toHabit = list.getByPosition(to);
            list.reorder(fromHabit, toHabit);

            int actualPositions[] = new int[10];

            for (int j = 0; j < 10; j++)
            {
                Habit h = list.getById(j);
                if (h == null) fail();
                actualPositions[j] = list.indexOf(h);
            }

            assertThat(actualPositions, equalTo(expectedPosition[i]));
        }
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        HabitList list = modelFactory.buildHabitList();

        Habit h1 = new Habit();
        h1.setName("Meditate");
        h1.setDescription("Did you meditate this morning?");
        h1.setFreqNum(1);
        h1.setFreqDen(1);
        h1.setColor(3);

        Habit h2 = new Habit();
        h2.setName("Wake up early");
        h2.setDescription("Did you wake up before 6am?");
        h2.setFreqNum(2);
        h2.setFreqDen(3);
        h2.setColor(5);

        list.add(h1);
        list.add(h2);

        String expectedCSV =
            "Position,Name,Description,NumRepetitions,Interval,Color\n" +
            "001,Meditate,Did you meditate this morning?,1,1,#AFB42B\n" +
            "002,Wake up early,Did you wake up before 6am?,2,3,#00897B\n";

        StringWriter writer = new StringWriter();
        list.writeCSV(writer);

        MatcherAssert.assertThat(writer.toString(), equalTo(expectedCSV));
    }
}