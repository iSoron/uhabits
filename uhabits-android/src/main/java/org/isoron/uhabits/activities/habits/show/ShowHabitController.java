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

package org.isoron.uhabits.activities.habits.show;

import android.support.annotation.*;

import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;

import javax.inject.*;

@ActivityScope
public class ShowHabitController
    implements ShowHabitRootView.Controller, HistoryEditorDialog.Controller
{
    @NonNull
    private final ShowHabitScreen screen;

    @NonNull
    private final Habit habit;

    @NonNull
    private final CommandRunner commandRunner;

    @Inject
    public ShowHabitController(@NonNull ShowHabitScreen screen,
                               @NonNull CommandRunner commandRunner,
                               @NonNull Habit habit)
    {
        this.screen = screen;
        this.habit = habit;
        this.commandRunner = commandRunner;
    }

    @Override
    public void onEditHistoryButtonClick()
    {
        screen.showEditHistoryDialog();
    }

    @Override
    public void onToggleCheckmark(long timestamp)
    {
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            null);
    }

    @Override
    public void onToolbarChanged()
    {
        screen.invalidateToolbar();
    }
}
