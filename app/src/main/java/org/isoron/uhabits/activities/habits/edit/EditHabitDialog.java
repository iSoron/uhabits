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

package org.isoron.uhabits.activities.habits.edit;

import android.os.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;

public class EditHabitDialog extends BaseDialog
{
    public static EditHabitDialog newInstance(Habit habit)
    {
        if(habit.getId() == null)
            throw new IllegalArgumentException("habit not saved");

        EditHabitDialog frag = new EditHabitDialog();
        Bundle args = new Bundle();
        args.putLong("habitId", habit.getId());
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected int getTitle()
    {
        return R.string.edit_habit;
    }

    @Override
    protected void initializeHabits()
    {
        Long habitId = (Long) getArguments().get("habitId");
        if (habitId == null)
            throw new IllegalArgumentException("habitId must be specified");

        originalHabit = habitList.getById(habitId);
        modifiedHabit = new Habit();
        modifiedHabit.copyFrom(originalHabit);
    }

    @Override
    protected void saveHabit()
    {
        Command command =
            new EditHabitCommand(habitList, originalHabit, modifiedHabit);
        commandRunner.execute(command, originalHabit.getId());
    }
}
