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

package org.isoron.uhabits.ui.common.dialogs;

import android.content.*;
import android.support.annotation.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.habits.edit.*;

import java.io.*;

import javax.inject.*;

public class DialogFactory
{
    private final Context context;

    @Inject
    public DialogFactory(@ActivityContext Context context)
    {
        this.context = context;
    }

    @NonNull
    public ColorPickerDialog buildColorPicker(int paletteColor)
    {
        return ColorPickerDialog.newInstance(context, paletteColor);
    }

    @NonNull
    public ConfirmDeleteDialog buildConfirmDeleteDialog(
        @NonNull ConfirmDeleteDialog.Callback callback)
    {
        return new ConfirmDeleteDialog(context, callback);
    }

    @NonNull
    public CreateHabitDialog buildCreateHabitDialog()
    {
        return new CreateHabitDialog();
    }

    @NonNull
    public EditHabitDialog buildEditHabitDialog(Habit habit)
    {
        return EditHabitDialog.newInstance(habit);
    }

    @NonNull
    public FilePickerDialog buildFilePicker(File dir)
    {
        return new FilePickerDialog(context, dir);
    }
}
