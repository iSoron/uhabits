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

import org.isoron.uhabits.BaseAndroidTest;
import org.isoron.uhabits.commands.CreateHabitCommand;
import org.isoron.uhabits.models.Habit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CreateHabitCommandTest extends BaseAndroidTest
{

    private CreateHabitCommand command;

    private Habit model;

    @Before
    public void setUp()
    {
        super.setUp();

        model = new Habit();
        model.setName("New habit");
        command = new CreateHabitCommand(model);

        fixtures.purgeHabits(habitList);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertTrue(habitList.getAll(true).isEmpty());

        command.execute();

        List<Habit> allHabits = habitList.getAll(true);
        assertThat(allHabits.size(), equalTo(1));

        Habit habit = allHabits.get(0);
        Long id = habit.getId();
        assertThat(habit.getName(), equalTo(model.getName()));

        command.undo();
        assertTrue(habitList.getAll(true).isEmpty());

        command.execute();
        allHabits = habitList.getAll(true);
        assertThat(allHabits.size(), equalTo(1));

        habit = allHabits.get(0);
        Long newId = habit.getId();
        assertThat(id, equalTo(newId));
        assertThat(habit.getName(), equalTo(model.getName()));
    }
}
