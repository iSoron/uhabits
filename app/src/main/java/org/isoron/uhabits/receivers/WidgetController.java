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

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.notifications.*;

import javax.inject.*;

@ReceiverScope
public class WidgetController
{
    @NonNull
    private final CommandRunner commandRunner;

    private NotificationTray notificationTray;

    @Inject
    public WidgetController(@NonNull CommandRunner commandRunner,
                            @NonNull NotificationTray notificationTray)
    {
        this.commandRunner = commandRunner;
        this.notificationTray = notificationTray;
    }

    public void onAddRepetition(@NonNull Habit habit, long timestamp)
    {
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep != null) return;
        performToggle(habit, timestamp);
        notificationTray.cancel(habit);
    }

    public void onRemoveRepetition(@NonNull Habit habit, long timestamp)
    {
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep == null) return;
        performToggle(habit, timestamp);
    }

    public void onToggleRepetition(@NonNull Habit habit, long timestamp)
    {
        performToggle(habit, timestamp);
    }

    private void performToggle(@NonNull Habit habit, long timestamp)
    {
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            habit.getId());
    }
}
