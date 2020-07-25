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

import android.content.*;

import androidx.annotation.NonNull;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.Preferences;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.core.ui.screens.habits.show.*;
import org.isoron.uhabits.intents.*;

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
    private final ConfirmDeleteDialogFactory confirmDeleteDialogFactory;

    @NonNull
    private final CheckmarkOptionPickerFactory checkmarkOptionPickerFactory;

    private final Lazy<ShowHabitBehavior> behavior;

    @NonNull
    private final IntentFactory intentFactory;

    @NonNull
    private final Preferences prefs;

    @Inject
    public ShowHabitScreen(@NonNull BaseActivity activity,
                           @NonNull Habit habit,
                           @NonNull ShowHabitRootView view,
                           @NonNull ShowHabitsMenu menu,
                           @NonNull ConfirmDeleteDialogFactory confirmDeleteDialogFactory,
                           @NonNull CheckmarkOptionPickerFactory checkmarkOptionPickerFactory,
                           @NonNull IntentFactory intentFactory,
                           @NonNull Lazy<ShowHabitBehavior> behavior,
                           @NonNull Preferences prefs)
    {
        super(activity);
        this.intentFactory = intentFactory;
        setMenu(menu);
        setRootView(view);

        this.habit = habit;
        this.behavior = behavior;
        this.confirmDeleteDialogFactory = confirmDeleteDialogFactory;
        this.checkmarkOptionPickerFactory = checkmarkOptionPickerFactory;
        this.prefs = prefs;
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
        if (prefs.isAdvancedCheckmarksEnabled())
        {
            CheckmarkList checkmarks = habit.getCheckmarks();
            int oldValue = checkmarks.getValues(timestamp, timestamp)[0];
            checkmarkOptionPickerFactory.create(habit.getName(), timestamp.toString(), oldValue,
                    newValue ->
                    {
                        behavior.get().onCreateRepetition(timestamp, newValue);
                    }).show();
        }
        else behavior.get().onToggleCheckmark(timestamp);
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
        Intent intent = intentFactory.startEditActivity(activity, habit);
        activity.startActivity(intent);
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
