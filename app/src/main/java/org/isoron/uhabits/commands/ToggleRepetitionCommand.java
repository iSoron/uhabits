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

import org.isoron.uhabits.models.*;

/**
 * Command to toggle a repetition.
 */
public class ToggleRepetitionCommand extends Command
{
    final long timestamp;

    @NonNull
    final Habit habit;

    public ToggleRepetitionCommand(@NonNull Habit habit, long timestamp)
    {
        super();
        this.timestamp = timestamp;
        this.habit = habit;
    }

    @Override
    public void execute()
    {
        habit.getRepetitions().toggleTimestamp(timestamp);
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
        execute();
    }

    public static class Record
    {
        @NonNull
        public String id;

        @NonNull
        public String event = "Toggle";

        public long habitId;

        public long timestamp;

        public Record(@NonNull ToggleRepetitionCommand command)
        {
            id = command.getId();
            Long habitId = command.habit.getId();
            if(habitId == null) throw new RuntimeException("Habit not saved");

            this.timestamp = command.timestamp;
            this.habitId = habitId;
        }

        public ToggleRepetitionCommand toCommand(@NonNull HabitList habitList)
        {
            Habit h = habitList.getById(habitId);
            if(h == null) throw new HabitNotFoundException();

            ToggleRepetitionCommand command;
            command = new ToggleRepetitionCommand(h, timestamp);
            command.setId(id);
            return command;
        }
    }
}