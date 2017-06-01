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

import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

/**
 * Command to archive a list of habits.
 */
public class ArchiveHabitsCommand extends Command
{
    final List<Habit> selected;

    final HabitList habitList;

    public ArchiveHabitsCommand(@NonNull HabitList habitList,
                                @NonNull List<Habit> selected)
    {
        super();
        this.habitList = habitList;
        this.selected = new LinkedList<>(selected);
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setArchived(true);
        habitList.update(selected);
    }

    @NonNull
    @Override
    public Record toRecord()
    {
        return new Record(this);
    }

    @Override
    public void undo()
    {
        for (Habit h : selected) h.setArchived(false);
        habitList.update(selected);
    }

    public static class Record
    {
        @NonNull
        public final String id;

        @NonNull
        public final String event = "Archive";

        @NonNull
        public final List<Long> habits;

        public Record(@NonNull ArchiveHabitsCommand command)
        {
            id = command.getId();
            habits = new LinkedList<>();
            for (Habit h : command.selected)
            {
                habits.add(h.getId());
            }
        }

        @NonNull
        public ArchiveHabitsCommand toCommand(@NonNull HabitList habitList)
        {
            List<Habit> selected = new LinkedList<>();
            for (Long id : this.habits) selected.add(habitList.getById(id));

            ArchiveHabitsCommand command;
            command = new ArchiveHabitsCommand(habitList, selected);
            command.setId(id);
            return command;
        }
    }
}