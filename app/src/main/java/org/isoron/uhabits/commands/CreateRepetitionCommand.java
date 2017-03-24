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
public class CreateRepetitionCommand extends Command
{
    @NonNull
    private final Habit habit;

    private final long timestamp;

    private final int value;

    private Repetition previousRep;

    private Repetition newRep;

    public CreateRepetitionCommand(@NonNull Habit habit,
                                   long timestamp,
                                   int value)
    {
        this.timestamp = timestamp;
        this.habit = habit;
        this.value = value;
    }

    @Override
    public void execute()
    {
        RepetitionList reps = habit.getRepetitions();

        previousRep = reps.getByTimestamp(timestamp);
        if (previousRep != null) reps.remove(previousRep);

        newRep = new Repetition(timestamp, value);
        reps.add(newRep);

        habit.invalidateNewerThan(timestamp);
    }

    @NonNull
    public Habit getHabit()
    {
        return habit;
    }

    @Override
    public void undo()
    {
        habit.getRepetitions().remove(newRep);
        if (previousRep != null) habit.getRepetitions().add(previousRep);
        habit.invalidateNewerThan(timestamp);
    }
}