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

package org.isoron.uhabits.activities.habits.list;

import android.app.*;
import android.content.*;
import android.net.*;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;
import android.text.*;
import android.view.*;
import android.widget.*;

import org.isoron.androidbase.activities.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.habits.edit.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.inject.*;

import static android.content.DialogInterface.*;
import static android.view.inputmethod.EditorInfo.*;

@ActivityScope
public class ListHabitsScreen extends BaseScreen
    implements CommandRunner.Listener, ListHabitsBehavior.Screen,
               ListHabitsMenuBehavior.Screen,
               ListHabitsSelectionMenuBehavior.Screen
{
    public static final int REQUEST_OPEN_DOCUMENT = 6;

    public static final int REQUEST_SETTINGS = 7;

    public static final int RESULT_BUG_REPORT = 4;

    public static final int RESULT_EXPORT_CSV = 2;

    public static final int RESULT_EXPORT_DB = 3;

    public static final int RESULT_IMPORT_DATA = 1;

    public static final int RESULT_REPAIR_DB = 5;

    @Nullable
    private ListHabitsController controller;

    @NonNull
    private final IntentFactory intentFactory;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final ConfirmDeleteDialogFactory confirmDeleteDialogFactory;

    @NonNull
    private final ColorPickerDialogFactory colorPickerFactory;

    @NonNull
    private final EditHabitDialogFactory editHabitDialogFactory;

    @NonNull
    private final ThemeSwitcher themeSwitcher;

    @NonNull
    private AndroidPreferences prefs;

    @Nullable
    private HabitCardListController listController;

    private final ListHabitsRootView rootView;

    @Inject
    public ListHabitsScreen(@NonNull BaseActivity activity,
                            @NonNull CommandRunner commandRunner,
                            @NonNull ListHabitsRootView rootView,
                            @NonNull IntentFactory intentFactory,
                            @NonNull ThemeSwitcher themeSwitcher,
                            @NonNull ConfirmDeleteDialogFactory confirmDeleteDialogFactory,
                            @NonNull ColorPickerDialogFactory colorPickerFactory,
                            @NonNull EditHabitDialogFactory editHabitDialogFactory,
                            @NonNull AndroidPreferences prefs)
    {
        super(activity);
        setRootView(rootView);
        this.rootView = rootView;
        this.prefs = prefs;
        this.colorPickerFactory = colorPickerFactory;
        this.commandRunner = commandRunner;
        this.confirmDeleteDialogFactory = confirmDeleteDialogFactory;
        this.editHabitDialogFactory = editHabitDialogFactory;
        this.intentFactory = intentFactory;
        this.themeSwitcher = themeSwitcher;
    }

    public void setListController(HabitCardListController listController)
    {
        this.listController = listController;
    }

    @StringRes
    private Integer getExecuteString(@NonNull Command command)
    {
        if(command instanceof ArchiveHabitsCommand)
            return R.string.toast_habit_archived;

        if(command instanceof ChangeHabitColorCommand)
            return R.string.toast_habit_changed;

        if(command instanceof CreateHabitCommand)
            return R.string.toast_habit_created;

        if(command instanceof DeleteHabitsCommand)
            return R.string.toast_habit_deleted;

        if(command instanceof EditHabitCommand)
            return R.string.toast_habit_changed;

        if(command instanceof UnarchiveHabitsCommand)
            return R.string.toast_habit_unarchived;

        return null;
    }

    public void onAttached()
    {
        commandRunner.addListener(this);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if (command.isRemote()) return;
        showMessage(getExecuteString(command));
    }

    public void onDettached()
    {
        commandRunner.removeListener(this);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_OPEN_DOCUMENT)
            onOpenDocumentResult(resultCode, data);

        if (requestCode == REQUEST_SETTINGS) onSettingsResult(resultCode);
    }

    public void setController(@Nullable ListHabitsController controller)
    {
        this.controller = controller;
    }

    @Override
    public void applyTheme()
    {
        themeSwitcher.apply();
        activity.restartWithFade(ListHabitsActivity.class);
    }

    @Override
    public void showAboutScreen()
    {
        Intent intent = intentFactory.startAboutActivity(activity);
        activity.startActivity(intent);
    }

    @Override
    public void showColorPicker(int defaultColor,
                                @NonNull OnColorPickedCallback callback) {
        ColorPickerDialog picker = colorPickerFactory.create(defaultColor);
        picker.setListener(callback);
        activity.showDialog(picker, "picker");
    }

    public void showCreateBooleanHabitScreen()
    {
        EditHabitDialog dialog;
        dialog = editHabitDialogFactory.createBoolean();
        activity.showDialog(dialog, "editHabit");
    }

    @Override
    public void showCreateHabitScreen()
    {
        if (!prefs.isNumericalHabitsFeatureEnabled())
        {
            showCreateBooleanHabitScreen();
            return;
        }

        Dialog dialog = new AlertDialog.Builder(activity)
            .setTitle("Type of habit")
            .setItems(R.array.habitTypes, (d, which) ->
            {
                if (which == 0) showCreateBooleanHabitScreen();
                else showCreateNumericalHabitScreen();
            })
            .create();

        dialog.show();
    }

    @Override
    public void showDeleteConfirmationScreen(
        @NonNull OnConfirmedCallback callback)
    {
        activity.showDialog(confirmDeleteDialogFactory.create(callback));
    }

    @Override
    public void showEditHabitsScreen(List<Habit> habits)
    {
        EditHabitDialog dialog;
        dialog = editHabitDialogFactory.edit(habits.get(0));
        activity.showDialog(dialog, "editNumericalHabit");
    }

    @Override
    public void showFAQScreen()
    {
        Intent intent = intentFactory.viewFAQ(activity);
        activity.startActivity(intent);
    }

    @Override
    public void showHabitScreen(@NonNull Habit habit)
    {
        Intent intent = intentFactory.startShowHabitActivity(activity, habit);
        activity.startActivity(intent);
    }

    public void showImportScreen()
    {
        Intent intent = intentFactory.openDocument();
        activity.startActivityForResult(intent, REQUEST_OPEN_DOCUMENT);
    }

    @Override
    public void showIntroScreen()
    {
        Intent intent = intentFactory.startIntroActivity(activity);
        activity.startActivity(intent);
    }

    @Override
    public void showMessage(@NonNull ListHabitsBehavior.Message m)
    {
        switch (m)
        {
            case COULD_NOT_EXPORT:
                showMessage(R.string.could_not_export);
                break;

            case IMPORT_SUCCESSFUL:
                showMessage(R.string.habits_imported);
                break;

            case IMPORT_FAILED:
                showMessage(R.string.could_not_import);
                break;

            case DATABASE_REPAIRED:
                showMessage(R.string.database_repaired);
                break;

            case COULD_NOT_GENERATE_BUG_REPORT:
                showMessage(R.string.bug_report_failed);
                break;

            case FILE_NOT_RECOGNIZED:
                showMessage(R.string.file_not_recognized);
                break;
        }
    }

    @Override
    public void showNumberPicker(double value,
                                 @NonNull String unit,
                                 @NonNull
                                     ListHabitsBehavior.NumberPickerCallback callback)
    {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        final NumberPicker picker;
        final NumberPicker picker2;
        final TextView tvUnit;

        picker = (NumberPicker) view.findViewById(R.id.picker);
        picker2 = (NumberPicker) view.findViewById(R.id.picker2);
        tvUnit = (TextView) view.findViewById(R.id.tvUnit);

        int intValue = (int) Math.round(value * 100);

        picker.setMinValue(0);
        picker.setMaxValue(Integer.MAX_VALUE / 100);
        picker.setValue(intValue / 100);
        picker.setWrapSelectorWheel(false);

        picker2.setMinValue(0);
        picker2.setMaxValue(19);
        picker2.setFormatter(v -> String.format("%02d", 5 * v));
        picker2.setValue((intValue % 100) / 5);
        refreshInitialValue(picker2);

        tvUnit.setText(unit);

        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setView(view)
            .setTitle(R.string.change_value)
            .setPositiveButton(android.R.string.ok, (d, which) ->
            {
                picker.clearFocus();
                double v = picker.getValue() + 0.05 * picker2.getValue();
                callback.onNumberPicked(v);
            })
            .create();

        InterfaceUtils.setupEditorAction(picker, (v, actionId, event) ->
        {
            if (actionId == IME_ACTION_DONE)
                dialog.getButton(BUTTON_POSITIVE).performClick();
            return false;
        });

        dialog.show();
    }

    @Override
    public void showSendBugReportToDeveloperScreen(String log)
    {
        int to = R.string.bugReportTo;
        int subject = R.string.bugReportSubject;
        showSendEmailScreen(to, subject, log);
    }

    @Override
    public void showSettingsScreen()
    {
        Intent intent = intentFactory.startSettingsActivity(activity);
        activity.startActivityForResult(intent, REQUEST_SETTINGS);
    }

    private void onOpenDocumentResult(int resultCode, Intent data)
    {
        if (controller == null) return;
        if (resultCode != Activity.RESULT_OK) return;

        try
        {
            Uri uri = data.getData();
            ContentResolver cr = activity.getContentResolver();
            InputStream is = cr.openInputStream(uri);

            File cacheDir = activity.getExternalCacheDir();
            File tempFile = File.createTempFile("import", "", cacheDir);

            FileUtils.copy(is, tempFile);
            controller.onImportData(tempFile, () -> tempFile.delete());
        }
        catch (IOException e)
        {
            showMessage(R.string.could_not_import);
            e.printStackTrace();
        }
    }

    private void onSettingsResult(int resultCode)
    {
        if (controller == null) return;

        switch (resultCode)
        {
            case RESULT_IMPORT_DATA:
                showImportScreen();
                break;

            case RESULT_EXPORT_CSV:
                controller.onExportCSV();
                break;

            case RESULT_EXPORT_DB:
                controller.onExportDB();
                break;

            case RESULT_BUG_REPORT:
                controller.onSendBugReport();
                break;

            case RESULT_REPAIR_DB:
                controller.onRepairDB();
                break;
        }
    }

    private void refreshInitialValue(NumberPicker picker2)
    {
        // Workaround for a bug on Android:
        // https://code.google.com/p/android/issues/detail?id=35482
        try
        {
            Field f = NumberPicker.class.getDeclaredField("mInputText");
            f.setAccessible(true);
            EditText inputText = (EditText) f.get(picker2);
            inputText.setFilters(new InputFilter[0]);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void showCreateNumericalHabitScreen()
    {
        EditHabitDialog dialog;
        dialog = editHabitDialogFactory.createNumerical();
        activity.showDialog(dialog, "editHabit");
    }
}
