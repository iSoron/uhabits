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
import android.support.v7.app.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.common.dialogs.*;
import org.isoron.uhabits.ui.common.dialogs.ColorPickerDialog.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.io.*;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class ListHabitsScreenTest extends BaseUnitTest
{
    private BaseActivity activity;

    private ListHabitsRootView rootView;

    private ListHabitsScreen screen;

    private ListHabitsController controller;

    private Habit habit;

    private Intent intent;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        activity = mock(BaseActivity.class);
        rootView = mock(ListHabitsRootView.class);
        controller = mock(ListHabitsController.class);
        intent = mock(Intent.class);

        habit = new Habit();
        screen = new ListHabitsScreen(activity, rootView);
        screen.setController(controller);
    }

    @Test
    public void testCreateHabitScreen()
    {
        CreateHabitDialog dialog = mock(CreateHabitDialog.class);
        when(dialogFactory.buildCreateHabitDialog()).thenReturn(dialog);

        screen.showCreateHabitScreen();

        verify(activity).showDialog(eq(dialog), any());
    }

    @Test
    public void testOnResult_bugReport()
    {
        screen.onResult(0, HabitsApplication.RESULT_BUG_REPORT, null);
        verify(controller).onSendBugReport();
    }

    @Test
    public void testOnResult_exportCSV()
    {
        screen.onResult(0, HabitsApplication.RESULT_EXPORT_CSV, null);
        verify(controller).onExportCSV();
    }

    @Test
    public void testOnResult_exportDB()
    {
        screen.onResult(0, HabitsApplication.RESULT_EXPORT_DB, null);
        verify(controller).onExportDB();
    }

    @Test
    public void testOnResult_importData()
    {
        screen.onResult(0, HabitsApplication.RESULT_IMPORT_DATA, null);
        testShowImportScreen();
    }

    @Test
    public void testShowAboutScreen() throws Exception
    {
        when(intentFactory.startAboutActivity(activity)).thenReturn(intent);
        screen.showAboutScreen();
        verify(activity).startActivity(eq(intent));
    }

    @Test
    public void testShowColorPicker()
    {
        habit.setColor(999);
        ColorPickerDialog picker = mock(ColorPickerDialog.class);
        when(dialogFactory.buildColorPicker(999)).thenReturn(picker);
        OnColorSelectedListener callback = mock(OnColorSelectedListener.class);

        screen.showColorPicker(habit, callback);

        verify(activity).showDialog(eq(picker), any());
        verify(picker).setListener(callback);
    }

    @Test
    public void testShowDeleteConfirmationScreen()
    {
        ConfirmDeleteDialog.Callback callback;
        callback = mock(ConfirmDeleteDialog.Callback.class);

        ConfirmDeleteDialog dialog = mock(ConfirmDeleteDialog.class);
        when(dialogFactory.buildConfirmDeleteDialog(activity,
            callback)).thenReturn(dialog);

        screen.showDeleteConfirmationScreen(callback);

        verify(activity).showDialog(dialog);
    }

    @Test
    public void testShowEditHabitScreen()
    {
        EditHabitDialog dialog = mock(EditHabitDialog.class);
        when(dialogFactory.buildEditHabitDialog(habit)).thenReturn(dialog);

        screen.showEditHabitScreen(habit);
        verify(activity).showDialog(eq(dialog), any());
    }

    @Test
    public void testShowFAQScreen()
    {
        when(intentFactory.viewFAQ(activity)).thenReturn(intent);
        screen.showFAQScreen();
        verify(activity).startActivity(intent);
    }

    @Test
    public void testShowHabitScreen()
    {
        when(intentFactory.startShowHabitActivity(activity, habit)).thenReturn(
            intent);
        screen.showHabitScreen(habit);
        verify(activity).startActivity(intent);
    }

    @Test
    public void testShowImportScreen()
    {
        File dir = mock(File.class);
        when(dirFinder.findStorageDir(any())).thenReturn(dir);

        FilePickerDialog picker = mock(FilePickerDialog.class);
        AppCompatDialog dialog = mock(AppCompatDialog.class);
        when(picker.getDialog()).thenReturn(dialog);
        when(dialogFactory.buildFilePicker(activity, dir)).thenReturn(picker);

        screen.showImportScreen();

        verify(activity).showDialog(dialog);
    }

    @Test
    public void testShowImportScreen_withInvalidPath()
    {
        when(dirFinder.findStorageDir(any())).thenReturn(null);
        screen.showImportScreen();
    }

    @Test
    public void testShowIntroScreen()
    {
        when(intentFactory.startIntroActivity(activity)).thenReturn(intent);
        screen.showIntroScreen();
        verify(activity).startActivity(intent);
    }

    @Test
    public void testShowSettingsScreen()
    {
        when(intentFactory.startSettingsActivity(activity)).thenReturn(intent);
        screen.showSettingsScreen();
        verify(activity).startActivityForResult(eq(intent), anyInt());
    }
}