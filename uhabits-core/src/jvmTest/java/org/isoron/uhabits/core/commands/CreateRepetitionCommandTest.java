/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import static org.isoron.uhabits.core.models.Entry.*;
import static org.junit.Assert.*;

public class CreateRepetitionCommandTest extends BaseUnitTest
{
    private CreateRepetitionCommand command;

    private Habit habit;

    private Timestamp today;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        habit = fixtures.createShortHabit();
        habitList.add(habit);

        today = DateUtils.getToday();
        command = new CreateRepetitionCommand(habitList, habit, today, 100);
    }

    @Test
    public void testExecute()
    {
        EntryList entries = habit.getOriginalEntries();
        Entry entry = entries.get(today);
        assertEquals(YES_MANUAL, entry.getValue());

        command.run();
        entry = entries.get(today);
        assertEquals(100, entry.getValue());
    }
}
