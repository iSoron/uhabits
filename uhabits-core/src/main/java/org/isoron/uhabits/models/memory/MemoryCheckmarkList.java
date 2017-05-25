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

import android.support.annotation.*;

import org.isoron.uhabits.models.*;

import java.util.*;

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
    public void add(List<Checkmark> checkmarks)
    {
        list.addAll(checkmarks);
        Collections.sort(list, (c1, c2) -> c2.compareNewer(c1));
    }

    @NonNull
    @Override
    public List<Checkmark> getByInterval(long fromTimestamp, long toTimestamp)
    {
        compute(fromTimestamp, toTimestamp);

        List<Checkmark> filtered = new LinkedList<>();

        for (Checkmark c : list)
            if (c.getTimestamp() >= fromTimestamp &&
                c.getTimestamp() <= toTimestamp) filtered.add(c);

        return filtered;
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
    protected Checkmark getOldestComputed()
    {
        if(list.isEmpty()) return null;
        return list.getLast();
    }

    @Override
    protected Checkmark getNewestComputed()
    {
        if(list.isEmpty()) return null;
        return list.getFirst();
    }

}
