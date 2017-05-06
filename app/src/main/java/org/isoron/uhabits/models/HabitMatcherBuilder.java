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

package org.isoron.uhabits.models;

import org.isoron.uhabits.utils.*;

import java.util.*;

public class HabitMatcherBuilder
{
    private boolean archivedAllowed = false;

    private boolean reminderRequired = false;

    private boolean completedAllowed = true;

    private List<Integer> allowedColors = allColors();

    private static List<Integer> allColors()
    {
        List<Integer> colors = new ArrayList<>();
        for(int i = 0; i < ColorUtils.CSV_PALETTE.length; i++)
            colors.add(i);
        return colors;
    }

    public HabitMatcher build()
    {
        return new HabitMatcher(archivedAllowed, reminderRequired,
            completedAllowed, allowedColors);
    }

    public HabitMatcherBuilder setArchivedAllowed(boolean archivedAllowed)
    {
        this.archivedAllowed = archivedAllowed;
        return this;
    }

    public HabitMatcherBuilder setAllowedColors(List<Integer> allowedColors)
    {
        this.allowedColors = allowedColors;
        return this;
    }

    public HabitMatcherBuilder setCompletedAllowed(boolean completedAllowed)
    {
        this.completedAllowed = completedAllowed;
        return this;
    }

    public HabitMatcherBuilder setReminderRequired(boolean reminderRequired)
    {
        this.reminderRequired = reminderRequired;
        return this;
    }
}