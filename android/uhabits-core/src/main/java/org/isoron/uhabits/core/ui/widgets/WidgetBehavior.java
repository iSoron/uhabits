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
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;
import org.jetbrains.annotations.*;

import javax.inject.*;

import static org.isoron.uhabits.core.models.Checkmark.*;

public class WidgetBehavior
{
    private HabitList habitList;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final NotificationTray notificationTray;

    @NonNull
    private final Preferences preferences;

    @Inject
    public WidgetBehavior(@NonNull HabitList habitList,
                          @NonNull CommandRunner commandRunner,
                          @NonNull NotificationTray notificationTray,
                          @NonNull Preferences preferences)
    {
        this.habitList = habitList;
        this.commandRunner = commandRunner;
        this.notificationTray = notificationTray;
        this.preferences = preferences;
    }

    public void onAddRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        setValue(habit, timestamp, YES_MANUAL);
    }

    public void onRemoveRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        notificationTray.cancel(habit);
        setValue(habit, timestamp, NO);
    }

    public void onToggleRepetition(@NonNull Habit habit, Timestamp timestamp)
    {
        int currentValue = habit.getOriginalCheckmarks().getValue(timestamp);
        int newValue;
        if(preferences.isSkipEnabled())
            newValue = Checkmark.Companion.nextToggleValueWithSkip(currentValue);
        else
            newValue = Checkmark.Companion.nextToggleValueWithoutSkip(currentValue);
        setValue(habit, timestamp, newValue);
        notificationTray.cancel(habit);
    }

    public void onIncrement(@NotNull Habit habit, @NotNull Timestamp timestamp, int amount) {
        int currentValue = habit.getComputedCheckmarks().getValues(timestamp, timestamp)[0];
        setValue(habit, timestamp, currentValue + amount);
        notificationTray.cancel(habit);
    }

    public void onDecrement(@NotNull Habit habit, @NotNull Timestamp timestamp, int amount) {
        int currentValue = habit.getComputedCheckmarks().getValues(timestamp, timestamp)[0];
        setValue(habit, timestamp, currentValue - amount);
        notificationTray.cancel(habit);
    }

    public void setValue(@NonNull Habit habit, Timestamp timestamp, int newValue) {
        commandRunner.execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, newValue),
                habit.getId());
    }
}
