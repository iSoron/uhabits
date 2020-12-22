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
public class EditHabitCommand implements Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final Habit original;

    @NonNull
    final Habit modified;

    final long savedId;

    final boolean hasFrequencyChanged;

    final boolean hasTargetChanged;

    public EditHabitCommand(@Provided @NonNull ModelFactory modelFactory,
                            @NonNull HabitList habitList,
                            @NonNull Habit original,
                            @NonNull Habit modified)
    {
        Long habitId = original.getId();
        if (habitId == null) throw new RuntimeException("Habit not saved");

        this.savedId = habitId;
        this.habitList = habitList;
        this.modified = modelFactory.buildHabit();
        this.original = modelFactory.buildHabit();

        this.modified.copyFrom(modified);
        this.original.copyFrom(original);

        Frequency originalFreq = this.original.getFrequency();
        Frequency modifiedFreq = this.modified.getFrequency();
        hasFrequencyChanged = (!originalFreq.equals(modifiedFreq));
        hasTargetChanged =
            (original.getTargetType() != modified.getTargetType() ||
             original.getTargetValue() != modified.getTargetValue());
    }

    @Override
    public void execute()
    {
        copyAttributes(this.modified);
    }

    private void copyAttributes(Habit model)
    {
        Habit habit = habitList.getById(savedId);
        if (habit == null) throw new RuntimeException("Habit not found");

        habit.copyFrom(model);
        habitList.update(habit);

        if (hasFrequencyChanged || hasTargetChanged)
            habit.invalidateNewerThan(Timestamp.ZERO);
    }

}