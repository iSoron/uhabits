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
import org.junit.*;
import org.junit.rules.*;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.isoron.uhabits.core.models.HabitList.Order.*;
import static org.junit.Assert.*;

@SuppressWarnings("JavaDoc")
public class HabitListTest extends BaseUnitTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ArrayList<Habit> habitsArray;

    private HabitList activeHabits;

    private HabitList reminderHabits;

    @Override
    public void setUp() throws Exception
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
    public void testSize()
    {
        assertThat(habitList.size(), equalTo(10));
        assertThat(activeHabits.size(), equalTo(6));
        assertThat(reminderHabits.size(), equalTo(4));
    }

    @Test
    public void testGetByPosition()
    {
        assertThat(habitList.getByPosition(0), equalTo(habitsArray.get(0)));
        assertThat(habitList.getByPosition(3), equalTo(habitsArray.get(3)));
        assertThat(habitList.getByPosition(9), equalTo(habitsArray.get(9)));

        assertThat(activeHabits.getByPosition(0), equalTo(habitsArray.get(2)));
        assertThat(reminderHabits.getByPosition(1),
            equalTo(habitsArray.get(3)));
    }

    @Test
    public void testGetById()
    {
        Habit habit1 = habitsArray.get(0);
        Habit habit2 = habitList.getById(habit1.getId());
        assertThat(habit1, equalTo(habit2));
    }

    @Test
    public void testGetById_withInvalidId()
    {
        assertNull(habitList.getById(100L));
    }

    @Test
    public void testOrdering()
    {
        HabitList list = modelFactory.buildHabitList();
        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("A Habit");
        h1.setColor(2);
        h1.setPosition(1);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setName("B Habit");
        h2.setColor(2);
        h2.setPosition(3);

        Habit h3 = fixtures.createEmptyHabit();
        h3.setName("C Habit");
        h3.setColor(0);
        h3.setPosition(0);

        Habit h4 = fixtures.createEmptyHabit();
        h4.setName("D Habit");
        h4.setColor(1);
        h4.setPosition(2);

        list.add(h3);
        list.add(h1);
        list.add(h4);
        list.add(h2);

        list.setOrder(BY_POSITION);
        assertThat(list.getByPosition(0), equalTo(h3));
        assertThat(list.getByPosition(1), equalTo(h1));
        assertThat(list.getByPosition(2), equalTo(h4));
        assertThat(list.getByPosition(3), equalTo(h2));

        list.setOrder(BY_NAME_DESC);
        assertThat(list.getByPosition(0), equalTo(h4));
        assertThat(list.getByPosition(1), equalTo(h3));
        assertThat(list.getByPosition(2), equalTo(h2));
        assertThat(list.getByPosition(3), equalTo(h1));

        list.setOrder(BY_NAME_ASC);
        assertThat(list.getByPosition(0), equalTo(h1));
        assertThat(list.getByPosition(1), equalTo(h2));
        assertThat(list.getByPosition(2), equalTo(h3));
        assertThat(list.getByPosition(3), equalTo(h4));

        list.remove(h1);
        list.add(h1);
        assertThat(list.getByPosition(0), equalTo(h1));

        list.setOrder(BY_COLOR_ASC);
        assertThat(list.getByPosition(0), equalTo(h3));
        assertThat(list.getByPosition(1), equalTo(h4));
        assertThat(list.getByPosition(2), equalTo(h1));
        assertThat(list.getByPosition(3), equalTo(h2));

        list.setOrder(BY_COLOR_DESC);
        assertThat(list.getByPosition(0), equalTo(h2));
        assertThat(list.getByPosition(1), equalTo(h1));
        assertThat(list.getByPosition(2), equalTo(h4));
        assertThat(list.getByPosition(3), equalTo(h3));

        list.setOrder(BY_POSITION);
        assertThat(list.getByPosition(0), equalTo(h3));
        assertThat(list.getByPosition(1), equalTo(h1));
        assertThat(list.getByPosition(2), equalTo(h4));
        assertThat(list.getByPosition(3), equalTo(h2));
    }

    @Test
    public void testReorder()
    {
        int operations[][] = {
            { 5, 2 }, { 3, 7 }, { 4, 4 }, { 8, 3 }
        };

        int expectedSequence[][] = {
            { 0, 1, 5, 2, 3, 4, 6, 7, 8, 9 },
            { 0, 1, 5, 2, 4, 6, 7, 3, 8, 9 },
            { 0, 1, 5, 2, 4, 6, 7, 3, 8, 9 },
            { 0, 1, 5, 2, 4, 6, 7, 8, 3, 9 },
        };

        for (int i = 0; i < operations.length; i++)
        {
            Habit fromHabit = habitsArray.get(operations[i][0]);
            Habit toHabit = habitsArray.get(operations[i][1]);
            habitList.reorder(fromHabit, toHabit);

            int actualSequence[] = new int[10];
            for (int j = 0; j < 10; j++)
            {
                Habit h = habitList.getByPosition(j);
                assertThat(h.getPosition(), equalTo(j));
                actualSequence[j] = toIntExact(h.getId());
            }

            assertThat(actualSequence, equalTo(expectedSequence[i]));
        }

        assertThat(activeHabits.indexOf(habitsArray.get(5)), equalTo(0));
        assertThat(activeHabits.indexOf(habitsArray.get(2)), equalTo(1));
    }

    @Test
    public void testReorder_withInvalidArguments() throws Exception
    {
        Habit h1 = habitsArray.get(0);
        Habit h2 = fixtures.createEmptyHabit();
        thrown.expect(IllegalArgumentException.class);
        habitList.reorder(h1, h2);
    }

    @Test
    public void testOrder_inherit()
    {
        habitList.setOrder(BY_COLOR_ASC);
        HabitList filteredList = habitList.getFiltered(new HabitMatcherBuilder()
                                                               .setArchivedAllowed(false)
                                                               .setCompletedAllowed(false)
                                                               .build());
        assertEquals(filteredList.getOrder(), BY_COLOR_ASC);
    }

    @Test
    public void testWriteCSV() throws IOException
    {
        HabitList list = modelFactory.buildHabitList();

        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("Meditate");
        h1.setQuestion("Did you meditate this morning?");
        h1.setDescription("this is a test description");
        h1.setFrequency(Frequency.DAILY);
        h1.setColor(3);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setName("Wake up early");
        h2.setQuestion("Did you wake up before 6am?");
        h2.setDescription("");
        h2.setFrequency(new Frequency(2, 3));
        h2.setColor(5);

        list.add(h1);
        list.add(h2);

        String expectedCSV =
            "Position,Name,Question,Description,NumRepetitions,Interval,Color\n" +
            "001,Meditate,Did you meditate this morning?,this is a test description,1,1,#FF8F00\n" +
            "002,Wake up early,Did you wake up before 6am?,,2,3,#AFB42B\n";

        StringWriter writer = new StringWriter();
        list.writeCSV(writer);

        assertThat(writer.toString(), equalTo(expectedCSV));
    }

    @Test
    public void testAdd() throws Exception
    {
        Habit h1 = fixtures.createEmptyHabit();
        assertFalse(h1.isArchived());
        assertNull(h1.getId());
        assertThat(habitList.indexOf(h1), equalTo(-1));

        habitList.add(h1);
        assertNotNull(h1.getId());
        assertThat(habitList.indexOf(h1), not(equalTo(-1)));
        assertThat(activeHabits.indexOf(h1), not(equalTo(-1)));
    }

    @Test
    public void testAdd_withFilteredList() throws Exception
    {
        thrown.expect(IllegalStateException.class);
        activeHabits.add(fixtures.createEmptyHabit());
    }

    @Test
    public void testRemove_onFilteredList() throws Exception
    {
        thrown.expect(IllegalStateException.class);
        activeHabits.remove(fixtures.createEmptyHabit());
    }

    @Test
    public void testReorder_onFilteredList() throws Exception
    {
        Habit h1 = fixtures.createEmptyHabit();
        Habit h2 = fixtures.createEmptyHabit();
        thrown.expect(IllegalStateException.class);
        activeHabits.reorder(h1, h2);
    }

    @Test
    public void testReorder_onSortedList() throws Exception
    {
        habitList.setOrder(BY_SCORE_DESC);
        Habit h1 = habitsArray.get(1);
        Habit h2 = habitsArray.get(2);
        thrown.expect(IllegalStateException.class);
        habitList.reorder(h1, h2);
    }
}