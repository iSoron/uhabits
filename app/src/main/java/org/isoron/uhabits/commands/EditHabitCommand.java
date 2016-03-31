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

public class EditHabitCommand extends Command
{
    private Habit original;
    private Habit modified;
    private long savedId;
    private boolean hasIntervalChanged;

    public EditHabitCommand(Habit original, Habit modified)
    {
        this.savedId = original.getId();
        this.modified = new Habit(modified);
        this.original = new Habit(original);

        hasIntervalChanged = (!this.original.freqDen.equals(this.modified.freqDen) ||
                !this.original.freqNum.equals(this.modified.freqNum));
    }

    public void execute()
    {
        Habit habit = Habit.get(savedId);
        if(habit == null) throw new RuntimeException("Habit not found");

        habit.copyAttributes(modified);
        habit.save();
        if (hasIntervalChanged)
        {
            habit.checkmarks.deleteNewerThan(0);
            habit.streaks.deleteNewerThan(0);
            habit.scores.invalidateNewerThan(0);
        }
    }

    public void undo()
    {
        Habit habit = Habit.get(savedId);
        if(habit == null) throw new RuntimeException("Habit not found");

        habit.copyAttributes(original);
        habit.save();
        if (hasIntervalChanged)
        {
            habit.checkmarks.deleteNewerThan(0);
            habit.streaks.deleteNewerThan(0);
            habit.scores.invalidateNewerThan(0);
        }
    }

    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_changed;
    }

    public Integer getUndoStringId()
    {
        return R.string.toast_habit_changed_back;
    }
}