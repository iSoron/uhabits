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

package org.isoron.uhabits.activities.habits.edit;

import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.models.*;

import javax.inject.*;

public class EditHabitDialogFactory
{
    @Inject
    public EditHabitDialogFactory()
    {
    }

    public EditHabitDialog create(@NonNull Habit habit)
    {
        if (habit.getId() == null)
            throw new IllegalArgumentException("habit not saved");

        EditHabitDialog dialog = new EditHabitDialog();
        Bundle args = new Bundle();
        args.putLong("habitId", habit.getId());
        dialog.setArguments(args);
        return dialog;
    }
}
