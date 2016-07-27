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

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.habits.edit.*;

public class ShowHabitScreen extends BaseScreen
{
    @NonNull
    private final Habit habit;

    private DialogFactory dialogFactory;

    public ShowHabitScreen(@NonNull BaseActivity activity,
                           @NonNull Habit habit,
                           ShowHabitRootView view)
    {
        super(activity);
        dialogFactory = activity.getComponent().getDialogFactory();

        this.habit = habit;
        setRootView(view);
    }

    public void showEditHabitDialog()
    {
        EditHabitDialog dialog = dialogFactory.buildEditHabitDialog(habit);
        activity.showDialog(dialog, "editHabit");
    }

    public void showEditHistoryDialog(
        @NonNull HistoryEditorDialog.Controller controller)
    {
        HistoryEditorDialog dialog = new HistoryEditorDialog();
        dialog.setHabit(habit);
        dialog.setController(controller);
        dialog.show(activity.getSupportFragmentManager(), "historyEditor");
    }
}
