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

import androidx.annotation.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.habits.edit.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.core.ui.screens.habits.show.*;

import javax.inject.*;

import dagger.*;

@ActivityScope
public class ShowHabitScreen extends BaseScreen
    implements ShowHabitMenuBehavior.Screen,
               ShowHabitBehavior.Screen,
               HistoryEditorDialog.Controller,
               ShowHabitRootView.Controller
{
    @NonNull
    private final Habit habit;

    @NonNull
    private final EditHabitDialogFactory editHabitDialogFactory;

    @NonNull
    private final ConfirmDeleteDialogFactory confirmDeleteDialogFactory;

    private final Lazy<ShowHabitBehavior> behavior;

    @Inject
    public ShowHabitScreen(@NonNull BaseActivity activity,
                           @NonNull Habit habit,
                           @NonNull ShowHabitRootView view,
                           @NonNull ShowHabitsMenu menu,
                           @NonNull EditHabitDialogFactory editHabitDialogFactory,
                           @NonNull ConfirmDeleteDialogFactory confirmDeleteDialogFactory,
                           @NonNull Lazy<ShowHabitBehavior> behavior)
    {
        super(activity);
        setMenu(menu);
        setRootView(view);

        this.habit = habit;
        this.behavior = behavior;
        this.editHabitDialogFactory = editHabitDialogFactory;
        this.confirmDeleteDialogFactory = confirmDeleteDialogFactory;
        view.setController(this);
    }

    @Override
    public void onEditHistoryButtonClick()
    {
        behavior.get().onEditHistory();
    }

    @Override
    public void onToggleCheckmark(Timestamp timestamp)
    {
        behavior.get().onToggleCheckmark(timestamp);
    }

    @Override
    public void onToolbarChanged()
    {
        invalidateToolbar();
    }

    @Override
    public void reattachDialogs()
    {
        super.reattachDialogs();
        HistoryEditorDialog historyEditor = (HistoryEditorDialog) activity
            .getSupportFragmentManager()
            .findFragmentByTag("historyEditor");
        if (historyEditor != null) historyEditor.setController(this);
    }

    @Override
    public void showEditHabitScreen(@NonNull Habit habit)
    {
        activity.showDialog(editHabitDialogFactory.edit(habit), "editHabit");
    }

    @Override
    public void showEditHistoryScreen()
    {
        HistoryEditorDialog dialog = new HistoryEditorDialog();
        dialog.setHabit(habit);
        dialog.setController(this);
        dialog.show(activity.getSupportFragmentManager(), "historyEditor");
    }

    @Override
    public void showMessage(ShowHabitMenuBehavior.Message m)
    {
        switch (m)
        {
            case COULD_NOT_EXPORT:
                showMessage(R.string.could_not_export);

            case HABIT_DELETED:
                showMessage(R.string.delete_habits_message);
        }
    }

    @Override
    public void showDeleteConfirmationScreen(@NonNull OnConfirmedCallback callback) {
        activity.showDialog(confirmDeleteDialogFactory.create(callback));
    }

    @Override
    public void close() {
        activity.finish();
    }
}
