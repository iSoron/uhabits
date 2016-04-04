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

package org.isoron.uhabits.unit.commands;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChangeHabitColorCommandTest extends BaseTest
{
    private ChangeHabitColorCommand command;
    private LinkedList<Habit> habits;

    @Before
    public void setup()
    {
        super.setup();

        habits = new LinkedList<>();

        for(int i = 0; i < 3; i ++)
        {
            Habit habit = HabitFixtures.createShortHabit();
            habit.color = ColorHelper.palette[i+1];
            habit.save();
            habits.add(habit);
        }

        command = new ChangeHabitColorCommand(habits, ColorHelper.palette[0]);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        checkOriginalColors();

        command.execute();
        checkNewColors();

        command.undo();
        checkOriginalColors();

        command.execute();
        checkNewColors();
    }

    private void checkOriginalColors()
    {
        int k = 0;
        for(Habit h : habits)
            assertThat(h.color, equalTo(ColorHelper.palette[++k]));
    }

    private void checkNewColors()
    {
        for(Habit h : habits)
            assertThat(h.color, equalTo(ColorHelper.palette[0]));
    }
}
