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
import org.junit.rules.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeleteHabitsCommandTest extends BaseUnitTest
{
    private DeleteHabitsCommand command;

    private LinkedList<Habit> selected;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        selected = new LinkedList<>();

        // Habits that should be deleted
        for (int i = 0; i < 3; i++)
        {
            Habit habit = fixtures.createShortHabit();
            habitList.add(habit);
            selected.add(habit);
        }

        // Extra habit that should not be deleted
        Habit extraHabit = fixtures.createShortHabit();
        extraHabit.setName("extra");
        habitList.add(extraHabit);

        command = new DeleteHabitsCommand(habitList, selected);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertThat(habitList.size(), equalTo(4));

        command.execute();
        assertThat(habitList.size(), equalTo(1));
        assertThat(habitList.getByPosition(0).getName(), equalTo("extra"));

        thrown.expect(UnsupportedOperationException.class);
        command.undo();
    }
}
