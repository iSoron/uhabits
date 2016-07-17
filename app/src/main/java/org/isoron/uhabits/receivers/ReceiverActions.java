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

package org.isoron.uhabits.receivers;

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;

import javax.inject.*;

public class ReceiverActions
{
    @Inject
    CommandRunner commandRunner;

    public ReceiverActions()
    {
        HabitsApplication.getComponent().inject(this);
    }

    public void add_repetition(@NonNull Habit habit, long timestamp)
    {
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep != null) return;
        toggle_repetition(habit, timestamp);
    }

    public void remove_repetition(@NonNull Habit habit, long timestamp)
    {
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep == null) return;
        toggle_repetition(habit, timestamp);
    }

    public void toggle_repetition(@NonNull Habit habit, long timestamp)
    {
        new SimpleTask(() -> {
            commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
                habit.getId());
        }).execute();
    }
}
