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

import static org.isoron.uhabits.activities.habits.edit.BooleanHabitDialog.*;

public class BooleanHabitDialogFactory
{
    @Inject
    public BooleanHabitDialogFactory()
    {
    }

    public BooleanHabitDialog create()
    {
        return new BooleanHabitDialog();
    }

    public BooleanHabitDialog edit(@NonNull Habit habit)
    {
        if (habit.getId() == null)
            throw new IllegalArgumentException("habit not saved");

        BooleanHabitDialog dialog = new BooleanHabitDialog();
        Bundle args = new Bundle();
        args.putLong(BUNDLE_HABIT_ID, habit.getId());
        dialog.setArguments(args);
        return dialog;
    }
}
