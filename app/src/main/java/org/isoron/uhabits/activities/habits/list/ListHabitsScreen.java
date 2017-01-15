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

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.common.dialogs.ColorPickerDialog.*;
import org.isoron.uhabits.activities.habits.edit.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

import javax.inject.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;

@ActivityScope
public class ListHabitsScreen extends BaseScreen
    implements CommandRunner.Listener
{
    public static final int RESULT_IMPORT_DATA = 1;

    public static final int RESULT_EXPORT_CSV = 2;

    public static final int RESULT_EXPORT_DB = 3;

    public static final int RESULT_BUG_REPORT = 4;

    public static final int RESULT_REPAIR_DB = 5;

    public static final int REQUEST_OPEN_DOCUMENT = 6;

    public static final int REQUEST_SETTINGS = 7;

    @Nullable
    private ListHabitsController controller;

    @NonNull
    private final IntentFactory intentFactory;

    @NonNull
    private final DirFinder dirFinder;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final ConfirmDeleteDialogFactory confirmDeleteDialogFactory;

    @NonNull
    private final CreateHabitDialogFactory createHabitDialogFactory;

    @NonNull
    private final FilePickerDialogFactory filePickerDialogFactory;

    @NonNull
    private final ColorPickerDialogFactory colorPickerFactory;

    @NonNull
    private final EditHabitDialogFactory editHabitDialogFactory;

    @NonNull
    private final ThemeSwitcher themeSwitcher;

    @Inject
    public ListHabitsScreen(@NonNull BaseActivity activity,
                            @NonNull CommandRunner commandRunner,
                            @NonNull DirFinder dirFinder,
                            @NonNull ListHabitsRootView rootView,
                            @NonNull IntentFactory intentFactory,
                            @NonNull ThemeSwitcher themeSwitcher,
                            @NonNull ConfirmDeleteDialogFactory confirmDeleteDialogFactory,
                            @NonNull CreateHabitDialogFactory createHabitDialogFactory,
                            @NonNull FilePickerDialogFactory filePickerDialogFactory,
                            @NonNull ColorPickerDialogFactory colorPickerFactory,
                            @NonNull EditHabitDialogFactory editHabitDialogFactory)
    {
        super(activity);
        setRootView(rootView);
        this.editHabitDialogFactory = editHabitDialogFactory;
        this.colorPickerFactory = colorPickerFactory;
        this.commandRunner = commandRunner;
        this.confirmDeleteDialogFactory = confirmDeleteDialogFactory;
        this.createHabitDialogFactory = createHabitDialogFactory;
        this.dirFinder = dirFinder;
        this.filePickerDialogFactory = filePickerDialogFactory;
        this.intentFactory = intentFactory;
        this.themeSwitcher = themeSwitcher;
    }

    public void onAttached()
    {
        commandRunner.addListener(this);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        showMessage(command.getExecuteStringId());
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

        if (requestCode == REQUEST_SETTINGS)
            onSettingsResult(resultCode);
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
        ColorPickerDialog picker = colorPickerFactory.create(habit.getColor());
        picker.setListener(callback);
        activity.showDialog(picker, "picker");
    }

    public void showCreateHabitScreen()
    {
        activity.showDialog(createHabitDialogFactory.create(), "editHabit");
    }

    public void showDeleteConfirmationScreen(ConfirmDeleteDialog.Callback callback)
    {
        activity.showDialog(confirmDeleteDialogFactory.create(callback));
    }

    public void showEditHabitScreen(Habit habit)
    {
        EditHabitDialog dialog = editHabitDialogFactory.create(habit);
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
        if (SDK_INT < KITKAT)
        {
            showImportScreenPreKitKat();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        activity.startActivityForResult(intent, REQUEST_OPEN_DOCUMENT);
    }

    public void showImportScreenPreKitKat()
    {
        File dir = dirFinder.findStorageDir(null);

        if (dir == null)
        {
            showMessage(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = filePickerDialogFactory.create(dir);

        if (controller != null)
            picker.setListener(file -> controller.onImportData(file, () -> {}));

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
        activity.startActivityForResult(intent, REQUEST_SETTINGS);
    }

    public void toggleNightMode()
    {
        themeSwitcher.toggleNightMode();
        activity.restartWithFade();
    }
}
