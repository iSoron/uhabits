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

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.commands.EditHabitCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class EditHabitCommandTest extends BaseTest
{

    private EditHabitCommand command;
    private Habit habit;
    private Habit modified;
    private Long id;

    @Before
    public void setup()
    {
        super.setup();

        habit = HabitFixtures.createShortHabit();
        habit.name = "original";
        habit.freqDen = 1;
        habit.freqNum = 1;
        habit.save();

        id = habit.getId();

        modified = new Habit(habit);
        modified.name = "modified";
    }

    @Test
    public void testExecuteUndoRedo()
    {
        command = new EditHabitCommand(habit, modified);

        int originalScore = habit.scores.getTodayValue();
        assertThat(habit.name, equalTo("original"));

        command.execute();
        refreshHabit();
        assertThat(habit.name, equalTo("modified"));
        assertThat(habit.scores.getTodayValue(), equalTo(originalScore));

        command.undo();
        refreshHabit();
        assertThat(habit.name, equalTo("original"));
        assertThat(habit.scores.getTodayValue(), equalTo(originalScore));

        command.execute();
        refreshHabit();
        assertThat(habit.name, equalTo("modified"));
        assertThat(habit.scores.getTodayValue(), equalTo(originalScore));
    }

    @Test
    public void testExecuteUndoRedo_withModifiedInterval()
    {
        modified.freqNum = 1;
        modified.freqDen = 7;
        command = new EditHabitCommand(habit, modified);

        int originalScore = habit.scores.getTodayValue();
        assertThat(habit.name, equalTo("original"));

        command.execute();
        refreshHabit();
        assertThat(habit.name, equalTo("modified"));
        assertThat(habit.scores.getTodayValue(), greaterThan(originalScore));

        command.undo();
        refreshHabit();
        assertThat(habit.name, equalTo("original"));
        assertThat(habit.scores.getTodayValue(), equalTo(originalScore));

        command.execute();
        refreshHabit();
        assertThat(habit.name, equalTo("modified"));
        assertThat(habit.scores.getTodayValue(), greaterThan(originalScore));
    }

    private void refreshHabit()
    {
        habit = Habit.get(id);
        assertTrue(habit != null);
    }
}
