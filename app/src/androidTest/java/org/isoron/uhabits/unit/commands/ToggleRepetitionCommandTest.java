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
import org.isoron.uhabits.commands.ToggleRepetitionCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ToggleRepetitionCommandTest extends BaseAndroidTest
{

    private ToggleRepetitionCommand command;
    private Habit habit;
    private long today;

    @Before
    public void setUp()
    {
        super.setUp();

        habit = habitFixtures.createShortHabit();

        today = DateUtils.getStartOfToday();
        command = new ToggleRepetitionCommand(habit, today);
    }

    @Test
    public void testExecuteUndoRedo()
    {
        assertTrue(habit.getRepetitions().containsTimestamp(today));

        command.execute();
        assertFalse(habit.getRepetitions().containsTimestamp(today));

        command.undo();
        assertTrue(habit.getRepetitions().containsTimestamp(today));

        command.execute();
        assertFalse(habit.getRepetitions().containsTimestamp(today));
    }
}
