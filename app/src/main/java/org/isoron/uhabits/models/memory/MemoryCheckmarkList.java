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

package org.isoron.uhabits.models.memory;

import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.CheckmarkList;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.DateUtils;

import java.util.Collections;
import java.util.LinkedList;

/**
 * In-memory implementation of {@link CheckmarkList}.
 */
public class MemoryCheckmarkList extends CheckmarkList
{
    LinkedList<Checkmark> list;

    public MemoryCheckmarkList(Habit habit)
    {
        super(habit);
        list = new LinkedList<>();
    }

    @Override
    public int[] getValues(long from, long to)
    {
        compute(from, to);
        if (from > to) return new int[0];

        int length = (int) ((to - from) / DateUtils.millisecondsInOneDay + 1);
        int values[] = new int[length];

        int k = 0;
        for (Checkmark c : list)
            if(c.getTimestamp() >= from && c.getTimestamp() <= to)
                values[k++] = c.getValue();

        return values;
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        LinkedList<Checkmark> invalid = new LinkedList<>();

        for (Checkmark c : list)
            if (c.getTimestamp() >= timestamp) invalid.add(c);

        list.removeAll(invalid);
    }

    @Override
    protected Checkmark getNewest()
    {
        long newestTimestamp = 0;
        Checkmark newestCheck = null;

        for (Checkmark c : list)
        {
            if (c.getTimestamp() > newestTimestamp)
            {
                newestCheck = c;
                newestTimestamp = c.getTimestamp();
            }
        }

        return newestCheck;
    }

    @Override
    protected void insert(long[] timestamps, int[] values)
    {
        for (int i = 0; i < timestamps.length; i++)
        {
            long t = timestamps[i];
            int v = values[i];
            list.add(new Checkmark(habit, t, v));
        }

        Collections.sort(list,
            (c1, c2) -> (int) (c2.getTimestamp() - c1.getTimestamp()));
    }
}
