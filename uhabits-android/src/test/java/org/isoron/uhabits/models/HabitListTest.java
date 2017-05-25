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

import org.hamcrest.*;
import org.isoron.uhabits.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.isoron.uhabits.models.HabitList.Order.*;

@SuppressWarnings("JavaDoc")
public class HabitListTest extends BaseUnitTest
{
    private ArrayList<Habit> habitsArray;

    private HabitList activeHabits;

    private HabitList reminderHabits;

    @Override
    public void setUp()
    {
        super.setUp();
        habitsArray = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            Habit habit = fixtures.createEmptyHabit();
            habitList.add(habit);
            habitsArray.add(habit);

            if (i % 3 == 0)
                habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        }

        habitsArray.get(0).setArchived(true);
        habitsArray.get(1).setArchived(true);
        habitsArray.get(4).setArchived(true);
        habitsArray.get(7).setArchived(true);

        activeHabits = habitList.getFiltered(new HabitMatcherBuilder().build());

        reminderHabits = habitList.getFiltered(new HabitMatcherBuilder()
            .setArchivedAllowed(true)
            .setReminderRequired(true)
            .build());
    }

    @Test
    public void test_size()
    {
        assertThat(habitList.size(), equalTo(10));
    }

    @Test
    public void test_countActive()
    {
        assertThat(activeHabits.size(), equalTo(6));
    }

    @Test
    public void test_getByPosition()
    {
        assertThat(habitList.getByPosition(0), equalTo(habitsArray.get(0)));
        assertThat(habitList.getByPosition(3), equalTo(habitsArray.get(3)));
        assertThat(habitList.getByPosition(9), equalTo(habitsArray.get(9)));

        assertThat(activeHabits.getByPosition(0), equalTo(habitsArray.get(2)));
    }

    @Test
    public void test_getHabitsWithReminder()
    {
        assertThat(reminderHabits.size(), equalTo(4));
        assertThat(reminderHabits.getByPosition(1),
            equalTo(habitsArray.get(3)));
    }

    @Test
    public void test_get_withInvalidId()
    {
        assertThat(habitList.getById(100L), is(nullValue()));
    }

    @Test
    public void test_get_withValidId()
    {
        Habit habit1 = habitsArray.get(0);
        Habit habit2 = habitList.getById(habit1.getId());
        assertThat(habit1, equalTo(habit2));
    }

    @Test
    public void test_reorder()
    {
        int operations[][] = {
            { 5, 2 }, { 3, 7 }, { 4, 4 }, { 3, 2 }
        };

        int expectedPosition[][] = {
            { 0, 1, 3, 4, 5, 2, 6, 7, 8, 9 },
            { 0, 1, 7, 3, 4, 2, 5, 6, 8, 9 },
            { 0, 1, 7, 3, 4, 2, 5, 6, 8, 9 },
            { 0, 1, 7, 2, 4, 3, 5, 6, 8, 9 },
        };

        for (int i = 0; i < operations.length; i++)
        {
            int from = operations[i][0];
            int to = operations[i][1];

            Habit fromHabit = habitList.getByPosition(from);
            Habit toHabit = habitList.getByPosition(to);
            habitList.reorder(fromHabit, toHabit);

            int actualPositions[] = new int[10];

            for (int j = 0; j < 10; j++)
            {
                Habit h = habitList.getById(j);
                if (h == null) fail();
                actualPositions[j] = habitList.indexOf(h);
            }

            assertThat(actualPositions, equalTo(expectedPosition[i]));
        }
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        HabitList list = modelFactory.buildHabitList();

        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("Meditate");
        h1.setDescription("Did you meditate this morning?");
        h1.setFrequency(Frequency.DAILY);
        h1.setColor(3);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setName("Wake up early");
        h2.setDescription("Did you wake up before 6am?");
        h2.setFrequency(new Frequency(2, 3));
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

    @Test
    public void test_ordering()
    {
        HabitList list = modelFactory.buildHabitList();
        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("A Habit");
        h1.setColor(2);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setName("B Habit");
        h2.setColor(2);

        Habit h3 = fixtures.createEmptyHabit();
        h3.setName("C Habit");
        h3.setColor(0);

        Habit h4 = fixtures.createEmptyHabit();
        h4.setName("D Habit");
        h4.setColor(1);

        list.add(h3);
        list.add(h1);
        list.add(h4);
        list.add(h2);

        list.setOrder(BY_POSITION);
        assertThat(list.getByPosition(0), equalTo(h3));
        assertThat(list.getByPosition(1), equalTo(h1));
        assertThat(list.getByPosition(2), equalTo(h4));
        assertThat(list.getByPosition(3), equalTo(h2));

        list.setOrder(BY_NAME);
        assertThat(list.getByPosition(0), equalTo(h1));
        assertThat(list.getByPosition(1), equalTo(h2));
        assertThat(list.getByPosition(2), equalTo(h3));
        assertThat(list.getByPosition(3), equalTo(h4));

        list.remove(h1);
        list.add(h1);
        assertThat(list.getByPosition(0), equalTo(h1));

        list.setOrder(BY_COLOR);
        assertThat(list.getByPosition(0), equalTo(h3));
        assertThat(list.getByPosition(1), equalTo(h4));
        assertThat(list.getByPosition(2), equalTo(h1));
        assertThat(list.getByPosition(3), equalTo(h2));
    }
}