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

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.hamcrest.*;
import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;
import java.util.*;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.isoron.uhabits.models.HabitList.Order.*;

@SuppressWarnings("JavaDoc")
@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitListTest extends BaseAndroidTest
{
    private ArrayList<Habit> habitsArray;

    private HabitList activeHabits;

    private HabitList reminderHabits;

    @Override
    public void setUp()
    {
        super.setUp();
        habitList.removeAll();

        habitsArray = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            Habit habit = fixtures.createEmptyHabit((long) i);
            habitsArray.add(habit);

            if (i % 3 == 0)
                habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));

            habitList.update(habit);
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
                assertNotNull(h);
                actualPositions[j] = habitList.indexOf(h);
            }

            assertThat(actualPositions, equalTo(expectedPosition[i]));
        }
    }

    @Test
    public void test_writeCSV() throws IOException
    {
        habitList.removeAll();

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

        habitList.update(h1);
        habitList.update(h2);

        String expectedCSV =
            "Position,Name,Description,NumRepetitions,Interval,Color\n" +
            "001,Meditate,Did you meditate this morning?,1,1,#AFB42B\n" +
            "002,Wake up early,Did you wake up before 6am?,2,3,#00897B\n";

        StringWriter writer = new StringWriter();
        habitList.writeCSV(writer);

        MatcherAssert.assertThat(writer.toString(), equalTo(expectedCSV));
    }

    @Test
    public void test_ordering()
    {
        habitList.removeAll();

        Habit h3 = fixtures.createEmptyHabit();
        h3.setName("C Habit");
        h3.setColor(0);
        habitList.update(h3);

        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("A Habit");
        h1.setColor(2);
        habitList.update(h1);

        Habit h4 = fixtures.createEmptyHabit();
        h4.setName("D Habit");
        h4.setColor(1);
        habitList.update(h4);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setName("B Habit");
        h2.setColor(2);
        habitList.update(h2);

        habitList.setOrder(BY_POSITION);
        assertThat(habitList.getByPosition(0), equalTo(h3));
        assertThat(habitList.getByPosition(1), equalTo(h1));
        assertThat(habitList.getByPosition(2), equalTo(h4));
        assertThat(habitList.getByPosition(3), equalTo(h2));

        habitList.setOrder(BY_NAME);
        assertThat(habitList.getByPosition(0), equalTo(h1));
        assertThat(habitList.getByPosition(1), equalTo(h2));
        assertThat(habitList.getByPosition(2), equalTo(h3));
        assertThat(habitList.getByPosition(3), equalTo(h4));

        habitList.remove(h1);
        habitList.add(h1);
        assertThat(habitList.getByPosition(0), equalTo(h1));

        habitList.setOrder(BY_COLOR);
        assertThat(habitList.getByPosition(0), equalTo(h3));
        assertThat(habitList.getByPosition(1), equalTo(h4));
        assertThat(habitList.getByPosition(2), equalTo(h1));
        assertThat(habitList.getByPosition(3), equalTo(h2));
    }
}