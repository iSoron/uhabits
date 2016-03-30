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

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.hamcrest.MatcherAssert;
import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitTest extends BaseTest
{
    @Before
    public void setup()
    {
        super.setup();
        HabitFixtures.purgeHabits();
    }

    @Test
    public void constructor_default()
    {
        Habit habit = new Habit();
        assertThat(habit.archived, is(0));
        assertThat(habit.highlight, is(0));

        assertThat(habit.reminderHour, is(nullValue()));
        assertThat(habit.reminderMin, is(nullValue()));

        assertThat(habit.reminderDays, is(not(nullValue())));
        assertThat(habit.streaks, is(not(nullValue())));
        assertThat(habit.scores, is(not(nullValue())));
        assertThat(habit.repetitions, is(not(nullValue())));
        assertThat(habit.checkmarks, is(not(nullValue())));
    }

    @Test
    public void constructor_habit()
    {
        Habit model = new Habit();
        model.archived = 1;
        model.highlight = 1;
        model.color = Color.BLACK;
        model.freqNum = 10;
        model.freqDen = 20;
        model.reminderDays = 1;
        model.reminderHour = 8;
        model.reminderMin = 30;
        model.position = 0;

        Habit habit = new Habit(model);
        assertThat(habit.archived, is(model.archived));
        assertThat(habit.highlight, is(model.highlight));
        assertThat(habit.color, is(model.color));
        assertThat(habit.freqNum, is(model.freqNum));
        assertThat(habit.freqDen, is(model.freqDen));
        assertThat(habit.reminderDays, is(model.reminderDays));
        assertThat(habit.reminderHour, is(model.reminderHour));
        assertThat(habit.reminderMin, is(model.reminderMin));
        assertThat(habit.position, is(model.position));
    }

    @Test
    public void get_withValidId()
    {
        Habit habit = new Habit();
        habit.save();

        Habit habit2 = Habit.get(habit.getId());
        assertThat(habit, equalTo(habit2));
    }

    @Test
    public void get_withInvalidId()
    {
        Habit habit = Habit.get(123456L);
        assertThat(habit, is(nullValue()));
    }

    @Test
    public void getAll_withoutArchived()
    {
        List<Habit> habits = new LinkedList<>();
        List<Habit> habitsWithArchived = new LinkedList<>();

        for(int i = 0; i < 10; i++)
        {
            Habit h = new Habit();

            if(i % 2 == 0)
                h.archived = 1;
            else
                habits.add(h);

            habitsWithArchived.add(h);
            h.save();
        }

        assertThat(habits, equalTo(Habit.getAll(false)));
        assertThat(habitsWithArchived, equalTo(Habit.getAll(true)));
    }

    @Test
    public void getByPosition()
    {
        List<Habit> habits = new LinkedList<>();

        for(int i = 0; i < 10; i++)
        {
            Habit h = new Habit();
            h.save();
            habits.add(h);
        }

        for(int i = 0; i < 10; i++)
        {
            Habit h = Habit.getByPosition(i);
            if(h == null) fail();
            assertThat(h, equalTo(habits.get(i)));
        }
    }

    @Test
    public void count()
    {
        for(int i = 0; i < 10; i++)
        {
            Habit h = new Habit();
            if(i % 2 == 0) h.archived = 1;
            h.save();
        }

        assertThat(Habit.count(), equalTo(5));
    }


    @Test
    public void countWithArchived()
    {
        for(int i = 0; i < 10; i++)
        {
            Habit h = new Habit();
            if(i % 2 == 0) h.archived = 1;
            h.save();
        }

        assertThat(Habit.countWithArchived(), equalTo(10));
    }

    @Test
    public void updateId()
    {
        Habit habit = new Habit();
        habit.name = "Hello World";
        habit.save();

        Long oldId = habit.getId();
        Long newId = 123456L;
        Habit.updateId(oldId, newId);

        Habit newHabit = Habit.get(newId);
        if(newHabit == null) fail();
        assertThat(newHabit, is(not(nullValue())));
        assertThat(newHabit.name, equalTo(habit.name));
    }

    @Test
    public void reorder()
    {
        List<Long> ids = new LinkedList<>();

        int n = 10;
        for (int i = 0; i < n; i++)
        {
            Habit h = new Habit();
            h.save();
            ids.add(h.getId());
            assertThat(h.position, is(i));
        }

        int operations[][] = {
                {5, 2},
                {3, 7},
                {4, 4},
                {3, 2}
        };

        int expectedPosition[][] = {
                {0, 1, 3, 4, 5, 2, 6, 7, 8, 9},
                {0, 1, 7, 3, 4, 2, 5, 6, 8, 9},
                {0, 1, 7, 3, 4, 2, 5, 6, 8, 9},
                {0, 1, 7, 2, 4, 3, 5, 6, 8, 9},
        };

        for(int i = 0; i < operations.length; i++)
        {
            int from = operations[i][0];
            int to = operations[i][1];

            Habit fromHabit = Habit.getByPosition(from);
            Habit toHabit = Habit.getByPosition(to);
            Habit.reorder(fromHabit, toHabit);

            int actualPositions[] = new int[n];

            for (int j = 0; j < n; j++)
            {
                Habit h = Habit.get(ids.get(j));
                if (h == null) fail();
                actualPositions[j] = h.position;
            }

            assertThat(actualPositions, equalTo(expectedPosition[i]));
        }
    }

    @Test
    public  void rebuildOrder()
    {
        List<Long> ids = new LinkedList<>();
        int originalPositions[] = { 0, 1, 1, 4, 6, 8, 10, 10, 13};

        for (int p : originalPositions)
        {
            Habit h = new Habit();
            h.position = p;
            h.save();
            ids.add(h.getId());
        }

        Habit.rebuildOrder();

        for (int i = 0; i < originalPositions.length; i++)
        {
            Habit h = Habit.get(ids.get(i));
            if(h == null) fail();
            assertThat(h.position, is(i));
        }
    }

    @Test
    public void getHabitsWithReminder()
    {
        List<Habit> habitsWithReminder = new LinkedList<>();

        for(int i = 0; i < 10; i++)
        {
            Habit habit = new Habit();
            if(i % 2 == 0)
            {
                habit.reminderDays = DateHelper.ALL_WEEK_DAYS;
                habit.reminderHour = 8;
                habit.reminderMin = 30;
                habitsWithReminder.add(habit);
            }
            habit.save();
        }

        assertThat(habitsWithReminder, equalTo(Habit.getHabitsWithReminder()));
    }

    @Test
    public void archive_unarchive()
    {
        List<Habit> allHabits = new LinkedList<>();
        List<Habit> archivedHabits = new LinkedList<>();
        List<Habit> unarchivedHabits = new LinkedList<>();

        for(int i = 0; i < 10; i++)
        {
            Habit habit = new Habit();
            habit.save();
            allHabits.add(habit);

            if(i % 2 == 0)
                archivedHabits.add(habit);
            else
                unarchivedHabits.add(habit);
        }

        Habit.archive(archivedHabits);
        assertThat(Habit.getAll(false), equalTo(unarchivedHabits));
        assertThat(Habit.getAll(true), equalTo(allHabits));

        Habit.unarchive(archivedHabits);
        assertThat(Habit.getAll(false), equalTo(allHabits));
        assertThat(Habit.getAll(true), equalTo(allHabits));
    }

    @Test
    public void setColor()
    {
        List<Habit> habits = new LinkedList<>();

        for(int i = 0; i < 10; i++)
        {
            Habit habit = new Habit();
            habit.color = i;
            habit.save();
            habits.add(habit);
        }

        int newColor = 100;
        Habit.setColor(habits, newColor);

        for(Habit h : habits)
            assertThat(h.color, equalTo(newColor));
    }

    @Test
    public void hasReminder_clearReminder()
    {
        Habit h = new Habit();
        assertThat(h.hasReminder(), is(false));

        h.reminderDays = DateHelper.ALL_WEEK_DAYS;
        h.reminderHour = 8;
        h.reminderMin = 30;
        assertThat(h.hasReminder(), is(true));

        h.clearReminder();
        assertThat(h.hasReminder(), is(false));
    }

    @Test
    public void writeCSV() throws IOException
    {
        HabitFixtures.createEmptyHabit();
        HabitFixtures.createNonDailyHabit();

        String expectedCSV =
                "Name,Description,NumRepetitions,Interval,Color\n" +
                "Meditate,Did you meditate this morning?,1,1,#AFB42B\n" +
                "Wake up early,Did you wake up before 6am?,2,3,#00897B\n";

        StringWriter writer = new StringWriter();
        Habit.writeCSV(Habit.getAll(true), writer);

        MatcherAssert.assertThat(writer.toString(), equalTo(expectedCSV));
    }
}
