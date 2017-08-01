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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ArchiveHabitsCommandTest extends BaseUnitTest
{
    private ArchiveHabitsCommand command;

    private Habit habit;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        habit = fixtures.createShortHabit();
        habitList.add(habit);

        command = new ArchiveHabitsCommand(habitList,
            Collections.singletonList(habit));
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertFalse(habit.isArchived());

        command.execute();
        assertTrue(habit.isArchived());

        command.undo();
        assertFalse(habit.isArchived());

        command.execute();
        assertTrue(habit.isArchived());
    }

    @Test
    public void testRecord()
    {
        ArchiveHabitsCommand.Record rec = command.toRecord();
        ArchiveHabitsCommand other = rec.toCommand(habitList);
        assertThat(other.selected, equalTo(command.selected));
        assertThat(other.getId(), equalTo(command.getId()));
    }
}
