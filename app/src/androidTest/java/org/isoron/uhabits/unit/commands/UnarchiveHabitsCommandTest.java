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
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.models.Habit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UnarchiveHabitsCommandTest extends BaseAndroidTest
{
    private UnarchiveHabitsCommand command;
    private Habit habit;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habit = habitFixtures.createShortHabit();
        habit.setArchived(1);
        habitList.update(habit);

        command = new UnarchiveHabitsCommand(Collections.singletonList(habit));
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
}
