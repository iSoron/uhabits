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

import org.isoron.uhabits.models.Habit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitTest
{
    @Before
    public void prepare()
    {
        HabitFixtures.purgeHabits();
    }

    @Test
    public void constructor_default()
    {
        Habit habit = new Habit();
        assertThat(habit.archived, is(0));
        assertThat(habit.highlight, is(0));

        assertThat(habit.reminderDays, is(nullValue()));
        assertThat(habit.reminderHour, is(nullValue()));
        assertThat(habit.reminderMin, is(nullValue()));

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
    public void reorderTest()
    {
        List<Long> ids = new LinkedList<>();

        for (int i = 0; i < 10; i++)
        {
            Habit h = new Habit();
            h.save();
            ids.add(h.getId());
            assertThat(h.position, is(i));
        }

        int from = 5, to = 2;
        int expectedPosition[] = {0, 1, 3, 4, 5, 2, 6, 7, 8, 9};

        Habit fromHabit = Habit.get(ids.get(from));
        Habit toHabit = Habit.get(ids.get(to));
        Habit.reorder(fromHabit, toHabit);

        for (int i = 0; i < 10; i++)
        {
            Habit h = Habit.get(ids.get(i));
            assertThat(h.position, is(expectedPosition[i]));
        }
    }

    @Test
    public  void rebuildOrderTest()
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
            assertThat(h.position, is(i));
        }
    }
}
