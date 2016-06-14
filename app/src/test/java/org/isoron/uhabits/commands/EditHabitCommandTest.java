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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class EditHabitCommandTest extends BaseUnitTest
{

    private EditHabitCommand command;

    private Habit habit;

    private Habit modified;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habit = fixtures.createShortHabit();
        habit.setName("original");
        habit.setFreqDen(1);
        habit.setFreqNum(1);

        modified = new Habit();
        modified.copyFrom(habit);
        modified.setName("modified");
    }

    @Test
    public void testExecuteUndoRedo()
    {
        command = new EditHabitCommand(habit, modified);

        int originalScore = habit.getScores().getTodayValue();
        assertThat(habit.getName(), equalTo("original"));

        command.execute();
        assertThat(habit.getName(), equalTo("modified"));
        assertThat(habit.getScores().getTodayValue(), equalTo(originalScore));

        command.undo();
        assertThat(habit.getName(), equalTo("original"));
        assertThat(habit.getScores().getTodayValue(), equalTo(originalScore));

        command.execute();
        assertThat(habit.getName(), equalTo("modified"));
        assertThat(habit.getScores().getTodayValue(), equalTo(originalScore));
    }

    @Test
    public void testExecuteUndoRedo_withModifiedInterval()
    {
        modified.setFreqNum(1);
        modified.setFreqDen(7);
        command = new EditHabitCommand(habit, modified);

        int originalScore = habit.getScores().getTodayValue();
        assertThat(habit.getName(), equalTo("original"));

        command.execute();
        assertThat(habit.getName(), equalTo("modified"));
        assertThat(habit.getScores().getTodayValue(),
            greaterThan(originalScore));

        command.undo();
        assertThat(habit.getName(), equalTo("original"));
        assertThat(habit.getScores().getTodayValue(), equalTo(originalScore));

        command.execute();
        assertThat(habit.getName(), equalTo("modified"));
        assertThat(habit.getScores().getTodayValue(),
            greaterThan(originalScore));
    }
}
