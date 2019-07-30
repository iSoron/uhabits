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

import android.support.annotation.NonNull;

import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.HabitNotFoundException;



/**
 * Command to update a habit.
 */
public class UpdateHabitCommand extends Command
{
    @NonNull
    private HabitList list;

    @NonNull
    final Habit habit;

    public UpdateHabitCommand(@NonNull HabitList list,
                              @NonNull Habit habit)
    {
        super();
        this.list = list;
        this.habit = habit;
    }

    @Override
    public void execute()
    {
        list.update(habit);
    }

    @NonNull
    public Habit getHabit()
    {
        return habit;
    }

    @Override
    @NonNull
    public Record toRecord()
    {
        return new Record(this);
    }

    @Override
    public void undo()
    {
        throw new RuntimeException("Update command cannot be undone");
    }

    public static class Record
    {
        @NonNull
        public String id;

        @NonNull
        public String event = "Update";

        public long habit;

        public Record(@NonNull UpdateHabitCommand command)
        {
            id = command.getId();
            Long habitId = command.habit.getId();
            if (habitId == null) throw new RuntimeException("Habit not saved");

            this.habit = habitId;
        }

        public UpdateHabitCommand toCommand(@NonNull HabitList habitList)
        {
            Habit h = habitList.getById(habit);
            if (h == null) throw new HabitNotFoundException();

            UpdateHabitCommand command;
            command = new UpdateHabitCommand(
                habitList, h);
            command.setId(id);
            return command;
        }
    }
}