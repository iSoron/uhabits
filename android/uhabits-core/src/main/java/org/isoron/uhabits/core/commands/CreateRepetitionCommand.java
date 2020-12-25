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

import org.isoron.uhabits.core.models.*;

import java.util.*;

public class CreateRepetitionCommand implements Command
{
    @NonNull
    final Habit habit;

    @NonNull
    final HabitList habitList;

    @NonNull
    final Timestamp timestamp;

    final int value;

    public CreateRepetitionCommand(@NonNull HabitList habitList,
                                   @NonNull Habit habit,
                                   @NonNull Timestamp timestamp,
                                   int value)
    {
        this.habitList = habitList;
        this.timestamp = timestamp;
        this.habit = habit;
        this.value = value;
    }

    @Override
    public void execute()
    {
        Entries checks = habit.getOriginalEntries();
        checks.add(new Entry(timestamp, value));
        habit.recompute();
        habitList.resort();
    }

    @NonNull
    public Habit getHabit()
    {
        return habit;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateRepetitionCommand that = (CreateRepetitionCommand) o;
        return value == that.value &&
                habit.equals(that.habit) &&
                habitList.equals(that.habitList) &&
                timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(habit, habitList, timestamp, value);
    }
}