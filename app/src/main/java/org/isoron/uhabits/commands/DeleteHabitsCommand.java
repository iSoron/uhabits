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

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import java.util.List;

public class DeleteHabitsCommand extends Command
{
    private List<Habit> habits;
    private Boolean hasOnlyOne;

    public DeleteHabitsCommand(List<Habit> habits)
    {
        this.habits = habits;
        this.hasOnlyOne = (habits.size() == 1) ? true : false;
    }

    @Override
    public void execute()
    {
        for(Habit h : habits)
            h.cascadeDelete();

        Habit.rebuildOrder();
    }

    @Override
    public void undo()
    {
        throw new UnsupportedOperationException();
    }

    public Integer getExecuteStringId()
    {
        if (this.hasOnlyOne) {
            return R.string.toast_habit_deleted;
        }
        else {
            return R.string.toast_habits_deleted;
        }
    }

    public Integer getUndoStringId()
    {
        if (this.hasOnlyOne){
            return R.string.toast_habit_restored;
        }
        else {
            return R.string.toast_habits_restored;
        }
    }
}
