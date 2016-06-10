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
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.models.Habit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DeleteHabitsCommandTest extends BaseAndroidTest
{
    private DeleteHabitsCommand command;

    private LinkedList<Habit> habits;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habitFixtures.purgeHabits(habitList);
        habits = new LinkedList<>();

        // Habits that should be deleted
        for (int i = 0; i < 3; i++)
        {
            Habit habit = habitFixtures.createShortHabit();
            habits.add(habit);
        }

        // Extra habit that should not be deleted
        Habit extraHabit = habitFixtures.createShortHabit();
        extraHabit.setName("extra");

        command = new DeleteHabitsCommand(habits);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertThat(habitList.getAll(true).size(), equalTo(4));

        command.execute();
        assertThat(habitList.getAll(true).size(), equalTo(1));
        assertThat(habitList.getAll(true).get(0).getName(), equalTo("extra"));

        thrown.expect(UnsupportedOperationException.class);
        command.undo();
    }
}
