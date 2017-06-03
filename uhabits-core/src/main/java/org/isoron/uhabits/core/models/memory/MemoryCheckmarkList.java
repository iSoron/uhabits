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

import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

import static org.isoron.uhabits.core.utils.DateUtils.*;

/**
 * In-memory implementation of {@link CheckmarkList}.
 */
public class MemoryCheckmarkList extends CheckmarkList
{
    ArrayList<Checkmark> list;

    public MemoryCheckmarkList(Habit habit)
    {
        super(habit);
        list = new ArrayList<>();
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
        compute();

        long newestTimestamp = Long.MIN_VALUE;
        long oldestTimestamp = Long.MAX_VALUE;

        Checkmark newest = getNewestComputed();
        Checkmark oldest = getOldestComputed();
        if(newest != null) newestTimestamp = newest.getTimestamp();
        if(oldest != null) oldestTimestamp = oldest.getTimestamp();

        List<Checkmark> filtered = new LinkedList<>();
        for(long time = toTimestamp; time >= fromTimestamp; time -= millisecondsInOneDay)
        {
            if(time > newestTimestamp || time < oldestTimestamp)
                filtered.add(new Checkmark(time, Checkmark.UNCHECKED));
            else
            {
                int offset = (int) ((newestTimestamp - time) / millisecondsInOneDay);
                filtered.add(list.get(offset));
            }
        }

        return filtered;
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        list.clear();
        observable.notifyListeners();
    }

    @Override
    @Nullable
    protected Checkmark getOldestComputed()
    {
        if(list.isEmpty()) return null;
        return list.get(list.size()-1);
    }

    @Override
    @Nullable
    protected Checkmark getNewestComputed()
    {
        if(list.isEmpty()) return null;
        return list.get(0);
    }

}
