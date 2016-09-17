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
 * In-memory implementation of {@link RepetitionList}.
 */
public class MemoryRepetitionList extends RepetitionList
{
    LinkedList<Repetition> list;

    public MemoryRepetitionList(Habit habit)
    {
        super(habit);
        list = new LinkedList<>();
    }

    @Override
    public void add(Repetition repetition)
    {
        list.add(repetition);
        observable.notifyListeners();
    }

    @Override
    public List<Repetition> getByInterval(long fromTimestamp, long toTimestamp)
    {
        LinkedList<Repetition> filtered = new LinkedList<>();

        for (Repetition r : list)
        {
            long t = r.getTimestamp();
            if (t >= fromTimestamp && t <= toTimestamp) filtered.add(r);
        }

        Collections.sort(filtered,
            (r1, r2) -> (int) (r1.getTimestamp() - r2.getTimestamp()));

        return filtered;
    }

    @Nullable
    @Override
    public Repetition getByTimestamp(long timestamp)
    {
        for (Repetition r : list)
            if (r.getTimestamp() == timestamp) return r;

        return null;
    }

    @Nullable
    @Override
    public Repetition getOldest()
    {
        long oldestTime = Long.MAX_VALUE;
        Repetition oldestRep = null;

        for (Repetition rep : list)
        {
            if (rep.getTimestamp() < oldestTime)
            {
                oldestRep = rep;
                oldestTime = rep.getTimestamp();
            }

        }

        return oldestRep;
    }

    @Nullable
    @Override
    public Repetition getNewest()
    {
        long newestTime = -1;
        Repetition newestRep = null;

        for (Repetition rep : list)
        {
            if (rep.getTimestamp() > newestTime)
            {
                newestRep = rep;
                newestTime = rep.getTimestamp();
            }

        }

        return newestRep;
    }

    @Override
    public void remove(@NonNull Repetition repetition)
    {
        list.remove(repetition);
        observable.notifyListeners();
    }

    @NonNull
    @Override
    public long getTotalCount()
    {
        return list.size();
    }
}
