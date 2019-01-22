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

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UnarchiveHabitsCommandTest extends BaseUnitTest
{
    private UnarchiveHabitsCommand command;
    private Habit habit;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        habit = fixtures.createShortHabit();
        habit.setArchived(true);
        habitList.add(habit);

        command = new UnarchiveHabitsCommand(habitList, Collections
            .singletonList
            (habit));
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertTrue(habit.isArchived());

        command.execute();
        assertFalse(habit.isArchived());

        command.undo();
        assertTrue(habit.isArchived());

        command.execute();
        assertFalse(habit.isArchived());
    }

    @Test
    public void testRecord()
    {
        UnarchiveHabitsCommand.Record rec = command.toRecord();
        UnarchiveHabitsCommand other = rec.toCommand(habitList);
        assertThat(other.selected, equalTo(command.selected));
        assertThat(other.getId(), equalTo(command.getId()));
    }
}
