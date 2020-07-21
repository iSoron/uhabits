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
 * In-memory implementation of {@link RepetitionList}.
 */
public class MemoryRepetitionList extends RepetitionList
{
    ArrayList<Repetition> list;

    public MemoryRepetitionList(Habit habit)
    {
        super(habit);
        list = new ArrayList<>();
    }

    @Override
    public void add(Repetition repetition)
    {
        list.add(repetition);
        observable.notifyListeners();
    }

    @Override
    public List<Repetition> getByInterval(Timestamp fromTimestamp, Timestamp toTimestamp)
    {
        ArrayList<Repetition> filtered = new ArrayList<>();

        for (Repetition r : list)
        {
            Timestamp t = r.getTimestamp();
            if (t.isOlderThan(fromTimestamp) || t.isNewerThan(toTimestamp)) continue;
            filtered.add(r);
        }

        Collections.sort(filtered,
            (r1, r2) -> r1.getTimestamp().compare(r2.getTimestamp()));

        return filtered;
    }

    @Nullable
    @Override
    public Repetition getByTimestamp(Timestamp timestamp)
    {
        for (Repetition r : list)
            if (r.getTimestamp().equals(timestamp)) return r;

        return null;
    }

    @Nullable
    @Override
    public Repetition getOldest()
    {
        Timestamp oldestTimestamp = Timestamp.ZERO.plus(1000000);
        Repetition oldestRep = null;

        for (Repetition rep : list)
        {
            if (rep.getTimestamp().isOlderThan(oldestTimestamp))
            {
                oldestRep = rep;
                oldestTimestamp = rep.getTimestamp();
            }
        }

        return oldestRep;
    }

    @Nullable
    @Override
    public Repetition getNewest()
    {
        Timestamp newestTimestamp = Timestamp.ZERO;
        Repetition newestRep = null;

        for (Repetition rep : list)
        {
            if (rep.getTimestamp().isNewerThan(newestTimestamp))
            {
                newestRep = rep;
                newestTimestamp = rep.getTimestamp();
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

    @Override
    public long getTotalSuccessfulCount()
    {
        int count = 0;
        for (Repetition rep : list) {
            if (rep.getValue() != Checkmark.SKIPPED_EXPLICITLY) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public void removeAll()
    {
        list.clear();
        getObservable().notifyListeners();
    }
}
