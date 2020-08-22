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
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.isoron.uhabits.core.models.Checkmark.*;
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
    public void testExecuteUndoRedo()
    {
        RepetitionList reps = habit.getRepetitions();

        Repetition rep = reps.getByTimestamp(today);
        assertNotNull(rep);
        assertEquals(CHECKED_EXPLICITLY, rep.getValue());

        command.execute();
        rep = reps.getByTimestamp(today);
        assertNotNull(rep);
        assertEquals(100, rep.getValue());

        command.undo();
        rep = reps.getByTimestamp(today);
        assertNotNull(rep);
        assertEquals(CHECKED_EXPLICITLY, rep.getValue());
    }

    @Test
    public void testRecord()
    {
        CreateRepetitionCommand.Record rec = command.toRecord();
        CreateRepetitionCommand other = rec.toCommand(habitList);

        assertThat(command.getId(), equalTo(other.getId()));
        assertThat(command.timestamp, equalTo(other.timestamp));
        assertThat(command.value, equalTo(other.value));
        assertThat(command.habit, equalTo(other.habit));
    }
}
