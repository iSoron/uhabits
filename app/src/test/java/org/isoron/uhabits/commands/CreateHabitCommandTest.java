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

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class CreateHabitCommandTest extends BaseUnitTest
{
    private CreateHabitCommand command;

    private Habit model;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        model = fixtures.createEmptyHabit();
        model.setName("New habit");

        command = new CreateHabitCommand(modelFactory, habitList, model);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertTrue(habitList.isEmpty());

        command.execute();

        assertThat(habitList.size(), equalTo(1));

        Habit habit = habitList.getByPosition(0);
        Long id = habit.getId();
        assertThat(habit.getName(), equalTo(model.getName()));

        command.undo();
        assertTrue(habitList.isEmpty());

        command.execute();
        assertThat(habitList.size(), equalTo(1));

        habit = habitList.getByPosition(0);
        Long newId = habit.getId();
        assertThat(id, equalTo(newId));
        assertThat(habit.getName(), equalTo(model.getName()));
    }


}
