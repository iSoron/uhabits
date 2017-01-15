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
import org.isoron.uhabits.activities.habits.edit.*;
import org.isoron.uhabits.models.*;

import javax.inject.*;

@ActivityScope
public class ShowHabitScreen extends BaseScreen
{
    @NonNull
    private final Habit habit;

    @Nullable
    private ShowHabitController controller;

    @NonNull
    private final EditHabitDialogFactory editHabitDialogFactory;

    @Inject
    public ShowHabitScreen(@NonNull BaseActivity activity,
                           @NonNull Habit habit,
                           @NonNull ShowHabitRootView view,
                           @NonNull EditHabitDialogFactory editHabitDialogFactory)
    {
        super(activity);
        setRootView(view);
        this.editHabitDialogFactory = editHabitDialogFactory;
        this.habit = habit;
    }

    public void setController(@NonNull ShowHabitController controller)
    {
        this.controller = controller;
    }

    public void reattachDialogs()
    {
        if(controller == null) throw new IllegalStateException();

        HistoryEditorDialog historyEditor = (HistoryEditorDialog) activity
            .getSupportFragmentManager()
            .findFragmentByTag("historyEditor");

        if (historyEditor != null)
            historyEditor.setController(controller);
    }

    public void showEditHabitDialog()
    {
        EditHabitDialog dialog = editHabitDialogFactory.create(habit);
        activity.showDialog(dialog, "editHabit");
    }

    public void showEditHistoryDialog()
    {
        if(controller == null) throw new IllegalStateException();

        HistoryEditorDialog dialog = new HistoryEditorDialog();
        dialog.setHabit(habit);
        dialog.setController(controller);
        dialog.show(activity.getSupportFragmentManager(), "historyEditor");
    }
}
