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

package org.isoron.uhabits.ui.habits.list;

import android.content.*;
import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.common.dialogs.*;
import org.isoron.uhabits.ui.common.dialogs.ColorPickerDialog.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

public class ListHabitsScreen extends BaseScreen
{
    @Nullable
    ListHabitsController controller;

    @NonNull
    private final DialogFactory dialogFactory;

    @NonNull
    private final IntentFactory intentFactory;

    @NonNull
    private final DirFinder dirFinder;

    public ListHabitsScreen(@NonNull BaseActivity activity,
                            @NonNull ListHabitsRootView rootView)
    {
        super(activity);
        setRootView(rootView);

        BaseComponent component = HabitsApplication.getComponent();
        dialogFactory = component.getDialogFactory();
        intentFactory = component.getIntentFactory();
        dirFinder = component.getDirFinder();
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data)
    {
        if (controller == null) return;

        switch (resultCode)
        {
            case HabitsApplication.RESULT_IMPORT_DATA:
                showImportScreen();
                break;

            case HabitsApplication.RESULT_EXPORT_CSV:
                controller.onExportCSV();
                break;

            case HabitsApplication.RESULT_EXPORT_DB:
                controller.onExportDB();
                break;

            case HabitsApplication.RESULT_BUG_REPORT:
                controller.onSendBugReport();
                break;
        }
    }

    public void setController(@Nullable ListHabitsController controller)
    {
        this.controller = controller;
    }

    public void showAboutScreen()
    {
        Intent intent = intentFactory.startAboutActivity(activity);
        activity.startActivity(intent);
    }

    /**
     * Displays a {@link ColorPickerDialog} to the user.
     * <p>
     * The selected color on the dialog is the color of the given habit.
     *
     * @param habit    the habit
     * @param callback
     */
    public void showColorPicker(@NonNull Habit habit,
                                @NonNull OnColorSelectedListener callback)
    {
        ColorPickerDialog picker =
            dialogFactory.buildColorPicker(habit.getColor());
        picker.setListener(callback);
        activity.showDialog(picker, "picker");
    }

    public void showCreateHabitScreen()
    {
        CreateHabitDialog dialog = dialogFactory.buildCreateHabitDialog();
        activity.showDialog(dialog, "editHabit");
    }

    public void showDeleteConfirmationScreen(ConfirmDeleteDialog.Callback callback)
    {
        ConfirmDeleteDialog dialog =
            dialogFactory.buildConfirmDeleteDialog(activity, callback);
        activity.showDialog(dialog);
    }

    public void showEditHabitScreen(Habit habit)
    {
        EditHabitDialog dialog = dialogFactory.buildEditHabitDialog(habit);
        activity.showDialog(dialog, "editHabit");
    }

    public void showFAQScreen()
    {
        Intent intent = intentFactory.viewFAQ(activity);
        activity.startActivity(intent);
    }

    public void showHabitScreen(@NonNull Habit habit)
    {
        Intent intent = intentFactory.startShowHabitActivity(activity, habit);
        activity.startActivity(intent);
    }

    public void showImportScreen()
    {
        File dir = dirFinder.findStorageDir(null);

        if (dir == null)
        {
            activity.showMessage(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = dialogFactory.buildFilePicker(activity, dir);
        if (controller != null)
            picker.setListener(file -> controller.onImportData(file));
        activity.showDialog(picker.getDialog());
    }

    public void showIntroScreen()
    {
        Intent intent = intentFactory.startIntroActivity(activity);
        activity.startActivity(intent);
    }

    public void showSettingsScreen()
    {
        Intent intent = intentFactory.startSettingsActivity(activity);
        activity.startActivityForResult(intent, 0);
    }

    public void toggleNightMode()
    {
        if (InterfaceUtils.isNightMode())
            InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_LIGHT);
        else InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_DARK);

        refreshTheme();
    }

    private void refreshTheme()
    {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity, ListHabitsScreen.class);

            activity.finish();
            activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
            activity.startActivity(intent);

        }, 500); // HACK: Let the menu disappear first
    }
}
