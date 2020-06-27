/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.widgets;

import androidx.annotation.*;

import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;

import javax.inject.*;

public class WidgetBehavior extends BaseWidgetBehavior
{


    private NotificationTray notificationTray;

    @Inject
    public WidgetBehavior(@NonNull HabitList habitList,
                          @NonNull CommandRunner commandRunner,
                          @NonNull NotificationTray notificationTray)
    {
        super(habitList, commandRunner);
        this.notificationTray = notificationTray;
    }

    public void onAddRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep != null) return;
        performToggle(habit, timestamp);
    }

    public void onRemoveRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep == null) return;
        performToggle(habit, timestamp);
    }

    public void onToggleRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        performToggle(habit, timestamp);
    }

    private void performToggle(@NonNull Habit habit, Timestamp timestamp)
    {
        getCommandRunner().execute(
            new ToggleRepetitionCommand(getHabitList(), habit, timestamp),
            habit.getId());
    }
}
