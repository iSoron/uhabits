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

import java.util.*;

public class WeekdayList
{
    public static WeekdayList EVERY_DAY = new WeekdayList(127);

    private final boolean[] weekdays;

    public WeekdayList(int packedList)
    {
        if(packedList == 0) packedList = 127;
        weekdays = new boolean[7];

        int current = 1;
        for (int i = 0; i < 7; i++)
        {
            if ((packedList & current) != 0) weekdays[i] = true;
            current = current << 1;
        }
    }

    public WeekdayList(boolean weekdays[])
    {
        boolean isEmpty = true;
        for(boolean b : weekdays) if(b) isEmpty = false;
        if(isEmpty) throw new IllegalArgumentException("empty list");

        this.weekdays = Arrays.copyOf(weekdays, 7);
    }

    public boolean[] toArray()
    {
        return weekdays;
    }

    public int toInteger()
    {
        int packedList = 0;
        int current = 1;

        for (int i = 0; i < 7; i++)
        {
            if (weekdays[i]) packedList |= current;
            current = current << 1;
        }

        return packedList;
    }
}
