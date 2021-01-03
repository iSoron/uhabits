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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class CreateHabitCommandTest extends BaseUnitTest
{
    private CreateHabitCommand command;

    private Habit model;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        model = fixtures.createEmptyHabit();
        model.setName("New habit");
        model.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));

        command = new CreateHabitCommand(modelFactory, habitList, model);
    }

    @Test
    public void testExecute()
    {
        assertTrue(habitList.isEmpty());
        command.run();
        assertThat(habitList.size(), equalTo(1));
        Habit habit = habitList.getByPosition(0);
        assertThat(habit.getName(), equalTo(model.getName()));
    }
}
