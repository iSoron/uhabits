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
import org.isoron.uhabits.utils.*;
import org.junit.*;

import static junit.framework.Assert.*;

public class ToggleRepetitionCommandTest extends BaseUnitTest
{

    private ToggleRepetitionCommand command;
    private Habit habit;
    private long today;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habit = fixtures.createShortHabit();

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
