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

import java.util.*;

/**
 * Command to unarchive a list of habits.
 */
public class UnarchiveHabitsCommand extends Command
{
    HabitList habitList;

    private List<Habit> habits;

    public UnarchiveHabitsCommand(HabitList habitList, List<Habit> selected)
    {
        this.habits = selected;
        this.habitList = habitList;
    }

    @Override
    public void execute()
    {
        for(Habit h : habits) h.setArchived(false);
        habitList.update(habits);
    }

    @Override
    public void undo()
    {
        for(Habit h : habits) h.setArchived(true);
        habitList.update(habits);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_unarchived;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_archived;
    }
}