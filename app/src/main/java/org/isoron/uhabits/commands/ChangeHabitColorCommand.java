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

package org.isoron.uhabits.commands;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;

import java.util.*;

/**
 * Command to change the color of a list of habits.
 */
public class ChangeHabitColorCommand extends Command
{
    HabitList habitList;

    List<Habit> selected;

    List<Integer> originalColors;

    Integer newColor;

    public ChangeHabitColorCommand(HabitList habitList,
                                   List<Habit> selected,
                                   Integer newColor)
    {
        this.habitList = habitList;
        this.selected = selected;
        this.newColor = newColor;
        this.originalColors = new ArrayList<>(selected.size());

        for (Habit h : selected) originalColors.add(h.getColor());
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setColor(newColor);
        habitList.update(selected);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_changed;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_changed;
    }

    @Override
    public void undo()
    {
        int k = 0;
        for (Habit h : selected) h.setColor(originalColors.get(k++));
        habitList.update(selected);
    }
}
