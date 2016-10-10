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

import android.support.annotation.*;

import com.google.auto.factory.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;

/**
 * Command to create a habit.
 */
@AutoFactory
public class CreateHabitCommand extends Command
{
    private ModelFactory modelFactory;

    HabitList habitList;

    private Habit model;

    private Long savedId;

    public CreateHabitCommand(@Provided @NonNull ModelFactory modelFactory,
                              @NonNull HabitList habitList,
                              @NonNull Habit model)
    {
        this.modelFactory = modelFactory;
        this.habitList = habitList;
        this.model = model;
    }

    @Override
    public void execute()
    {
        Habit savedHabit = modelFactory.buildHabit();
        savedHabit.copyFrom(model);
        savedHabit.setId(savedId);

        habitList.add(savedHabit);
        savedId = savedHabit.getId();
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_created;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_deleted;
    }

    @Override
    public void undo()
    {
        Habit habit = habitList.getById(savedId);
        if (habit == null) throw new RuntimeException("Habit not found");

        habitList.remove(habit);
    }

}