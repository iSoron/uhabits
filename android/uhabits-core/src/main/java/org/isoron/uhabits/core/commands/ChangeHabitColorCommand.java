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

package org.isoron.uhabits.core.commands;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

/**
 * Command to change the color of a list of habits.
 */
public class ChangeHabitColorCommand implements Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    @NonNull
    final PaletteColor newColor;

    public ChangeHabitColorCommand(@NonNull HabitList habitList,
                                   @NonNull List<Habit> selected,
                                   @NonNull PaletteColor newColor)
    {
        this.habitList = habitList;
        this.selected = selected;
        this.newColor = newColor;
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setColor(newColor);
        habitList.update(selected);
    }
}
