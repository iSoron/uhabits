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

public class WidgetBehavior
{
    private HabitList habitList;

    @NonNull
    private final CommandRunner commandRunner;

    private NotificationTray notificationTray;

    @Inject
    public WidgetBehavior(@NonNull HabitList habitList,
                          @NonNull CommandRunner commandRunner,
                          @NonNull NotificationTray notificationTray)
    {
        this.habitList = habitList;
        this.commandRunner = commandRunner;
        this.notificationTray = notificationTray;
    }

    public void onAddRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep != null) return;
        performToggle(habit, timestamp, Checkmark.YES_MANUAL);
    }

    public void onRemoveRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        Repetition rep = habit.getRepetitions().getByTimestamp(timestamp);
        if (rep == null) return;
        performToggle(habit, timestamp, Checkmark.NO);
    }

    public void onToggleRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        Repetition previous = habit.getRepetitions().getByTimestamp(timestamp);
        if(previous == null) performToggle(habit, timestamp, Checkmark.YES_MANUAL);
        else performToggle(habit, timestamp, Repetition.nextToggleValue(previous.getValue()));
    }

    private void performToggle(@NonNull Habit habit, Timestamp timestamp, int value)
    {
        commandRunner.execute(
            new CreateRepetitionCommand(habitList, habit, timestamp, value),
            habit.getId());
    }

    public void setNumericValue(@NonNull Habit habit, Timestamp timestamp, int newValue) {
        commandRunner.execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, newValue),
                habit.getId());
    }

}
