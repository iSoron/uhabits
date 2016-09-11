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
 * Command to delete a list of habits.
 */
public class DeleteHabitsCommand extends Command
{
    HabitList habitList;

    private List<Habit> habits;

    public DeleteHabitsCommand(HabitList habitList, List<Habit> habits)
    {
        this.habits = habits;
        this.habitList = habitList;
    }

    @Override
    public void execute()
    {
        for (Habit h : habits)
            habitList.remove(h);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_deleted;
    }

    public List<Habit> getHabits()
    {
        return new LinkedList<>(habits);
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_restored;
    }

    @Override
    public void undo()
    {
        throw new UnsupportedOperationException();
    }
}
