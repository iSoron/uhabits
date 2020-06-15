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

import androidx.annotation.NonNull;

import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.Repetition;
import org.isoron.uhabits.core.models.RepetitionList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ResetHabitsCommand extends Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    public ResetHabitsCommand(@NonNull HabitList habitList, @NonNull List<Habit> selected) {
        this.habitList = habitList;
        this.selected = new LinkedList<>(selected);
    }

    public ResetHabitsCommand(@NonNull HabitList habitList) {
        this.habitList = habitList;
        selected = null;
    }

    @Override
    public void execute()
    {
        if (selected != null) {
            for (Habit h : selected) {
                RepetitionList repetitionList = h.getRepetitions();
                Repetition rep;
                while(repetitionList.getTotalCount() != 0) {
                    rep = repetitionList.getOldest();
                    repetitionList.toggle(rep.getTimestamp());
                    repetitionList.remove(rep);
                }
            }
            habitList.update(selected);
        } else {
            for (Habit h : habitList) {
                RepetitionList repetitionList = h.getRepetitions();
                Repetition rep;
                while(repetitionList.getTotalCount() != 0) {
                    rep = repetitionList.getOldest();
                    repetitionList.toggle(rep.getTimestamp());
                    repetitionList.remove(rep);
                }
            }
        }
    }


    public List<Habit> getSelected()
    {
        return Collections.unmodifiableList(selected);
    }

    @NonNull
    @Override
    public Object toRecord() {return new Record(this);}

    public static class Record {
        @NonNull
        public String id;

        @NonNull
        public String event = "ResetHabit";

        @NonNull
        public List<Long> habits;

        public Record(ResetHabitsCommand command) {
            id = command.getId();
            habits = new LinkedList<>();
            for (Habit h : command.selected) {
                if (!h.hasId()) throw new RuntimeException("Habit not saved");
                habits.add(h.getId());
            }
        }

        public ResetHabitsCommand toCommand(@NonNull HabitList habitList) {
            List<Habit> selected = new LinkedList<>();
            for (Long id : this.habits) selected.add(habitList.getById(id));

            ResetHabitsCommand command;
            command = new ResetHabitsCommand(habitList, selected);
            command.setId(id);
            return command;
        }
    }
    @Override
    public void undo() { throw new UnsupportedOperationException();}
}
