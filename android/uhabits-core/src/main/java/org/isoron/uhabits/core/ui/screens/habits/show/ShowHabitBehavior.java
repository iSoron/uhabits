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

package org.isoron.uhabits.core.ui.screens.habits.show;

import androidx.annotation.*;

import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;

import javax.inject.*;

public class ShowHabitBehavior
{
    private HabitList habitList;

    @NonNull
    private final Habit habit;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private Screen screen;

    @Inject
    public ShowHabitBehavior(@NonNull HabitList habitList,
                             @NonNull CommandRunner commandRunner,
                             @NonNull Habit habit,
                             @NonNull Screen screen)
    {
        this.habitList = habitList;
        this.habit = habit;
        this.commandRunner = commandRunner;
        this.screen = screen;
    }

    public void onEditHistory()
    {
        screen.showEditHistoryScreen();
    }

    public void onToggleCheckmark(Timestamp timestamp, int value)
    {
        if (habit.isNumerical()) {
            CheckmarkList checkmarks = habit.getCheckmarks();
            double oldValue = checkmarks.getValues(timestamp, timestamp)[0];

            screen.showNumberPicker(oldValue / 1000, habit.getUnit(), newValue ->
            {
                newValue = Math.round(newValue * 1000);
                commandRunner.execute(
                        new CreateRepetitionCommand(habitList, habit, timestamp, (int) newValue),
                        habit.getId());
            });
        } else {
            commandRunner.execute(
                    new CreateRepetitionCommand(habitList, habit, timestamp, value), null);
        }
    }

    public interface Screen
    {
        void showEditHistoryScreen();

        void showNumberPicker(double value,
                              @NonNull String unit,
                              @NonNull ListHabitsBehavior.NumberPickerCallback callback);
    }
}
