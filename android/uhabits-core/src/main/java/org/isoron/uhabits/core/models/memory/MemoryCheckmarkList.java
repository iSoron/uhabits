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

package org.isoron.uhabits.core.models.memory;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

/**
 * In-memory implementation of {@link CheckmarkList}.
 */
public class MemoryCheckmarkList extends CheckmarkList
{
    ArrayList<Entry> list;

    public MemoryCheckmarkList(Habit habit)
    {
        super(habit);
        list = new ArrayList<>();
    }

    @Override
    public void add(List<Entry> entries)
    {
        list.addAll(entries);
        Collections.sort(list,
            (c1, c2) -> c2.getTimestamp().compare(c1.getTimestamp()));
    }

    @NonNull
    @Override
    public synchronized List<Entry> getByInterval(Timestamp from,
                                                  Timestamp to)
    {
        compute();

        Timestamp newestComputed = new Timestamp(0);
        Timestamp oldestComputed = new Timestamp(0).plus(1000000);

        Entry newest = getNewestComputed();
        Entry oldest = getOldestComputed();
        if(newest != null) newestComputed = newest.getTimestamp();
        if(oldest != null) oldestComputed = oldest.getTimestamp();

        List<Entry> filtered = new ArrayList<>(
            Math.max(0, oldestComputed.daysUntil(newestComputed) + 1));

        for(int i = 0; i <= from.daysUntil(to); i++)
        {
            Timestamp t = to.minus(i);
            if(t.isNewerThan(newestComputed) || t.isOlderThan(oldestComputed))
                filtered.add(new Entry(t, Entry.UNKNOWN));
            else
                filtered.add(list.get(t.daysUntil(newestComputed)));
        }

        return filtered;
    }

    @Override
    public void invalidateNewerThan(Timestamp timestamp)
    {
        list.clear();
        observable.notifyListeners();
    }

    @Override
    @Nullable
    protected Entry getOldestComputed()
    {
        if(list.isEmpty()) return null;
        return list.get(list.size()-1);
    }

    @Override
    @Nullable
    protected Entry getNewestComputed()
    {
        if(list.isEmpty()) return null;
        return list.get(0);
    }

}
