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

package org.isoron.uhabits.core.commands;

import androidx.annotation.*;

import com.google.auto.factory.*;

import org.isoron.uhabits.core.models.*;

@AutoFactory
public class CreateHabitCommand implements Command
{
    ModelFactory modelFactory;

    HabitList habitList;

    @NonNull
    Habit model;

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
        Habit habit = modelFactory.buildHabit();
        habit.copyFrom(model);
        habitList.add(habit);
    }
}