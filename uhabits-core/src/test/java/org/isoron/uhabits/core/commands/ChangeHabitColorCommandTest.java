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

package org.isoron.uhabits.core.commands;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ChangeHabitColorCommandTest extends BaseUnitTest
{
    private ChangeHabitColorCommand command;

    private LinkedList<Habit> selected;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        selected = new LinkedList<>();

        for (int i = 0; i < 3; i++)
        {
            Habit habit = fixtures.createShortHabit();
            habit.setColor(i + 1);
            selected.add(habit);
            habitList.add(habit);
        }

        command = new ChangeHabitColorCommand(habitList, selected, 0);
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

    @Test
    public void testRecord()
    {
        ChangeHabitColorCommand.Record rec = command.toRecord();
        ChangeHabitColorCommand other = rec.toCommand(habitList);
        assertThat(other.getId(), equalTo(command.getId()));
        assertThat(other.newColor, equalTo(command.newColor));
        assertThat(other.selected, equalTo(command.selected));
    }

    private void checkNewColors()
    {
        for (Habit h : selected)
            assertThat(h.getColor(), equalTo(0));
    }

    private void checkOriginalColors()
    {
        int k = 0;
        for (Habit h : selected)
            assertThat(h.getColor(), equalTo(++k));
    }
}
