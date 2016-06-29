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

package org.isoron.uhabits.commands;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ChangeHabitColorCommandTest extends BaseUnitTest
{
    private ChangeHabitColorCommand command;

    private LinkedList<Habit> habits;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habits = new LinkedList<>();

        for (int i = 0; i < 3; i++)
        {
            Habit habit = fixtures.createShortHabit();
            habit.setColor(i + 1);
            habits.add(habit);
        }

        command = new ChangeHabitColorCommand(habitList, habits, 0);
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

    private void checkNewColors()
    {
        for (Habit h : habits)
            assertThat(h.getColor(), equalTo(0));
    }

    private void checkOriginalColors()
    {
        int k = 0;
        for (Habit h : habits)
            assertThat(h.getColor(), equalTo(++k));
    }
}
