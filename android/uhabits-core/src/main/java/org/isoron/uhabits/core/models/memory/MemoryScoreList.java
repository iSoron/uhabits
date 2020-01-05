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

public class MemoryScoreList extends ScoreList
{
    ArrayList<Score> list;

    public MemoryScoreList(Habit habit)
    {
        super(habit);
        list = new ArrayList<>();
    }

    @Override
    public void add(List<Score> scores)
    {
        list.addAll(scores);
        Collections.sort(list,
            (s1, s2) -> s2.getTimestamp().compare(s1.getTimestamp()));
        getObservable().notifyListeners();
    }

    @NonNull
    @Override
    public List<Score> getByInterval(@NonNull Timestamp fromTimestamp,
                                     @NonNull Timestamp toTimestamp)
    {
        compute(fromTimestamp, toTimestamp);

        List<Score> filtered = new LinkedList<>();

        for (Score s : list)
        {
            if (s.getTimestamp().isNewerThan(toTimestamp) ||
                s.getTimestamp().isOlderThan(fromTimestamp)) continue;
            filtered.add(s);
        }

        return filtered;
    }

    @Nullable
    @Override
    public Score getComputedByTimestamp(Timestamp timestamp)
    {
        for (Score s : list)
            if (s.getTimestamp().equals(timestamp)) return s;

        return null;
    }

    @Override
    public void invalidateNewerThan(Timestamp timestamp)
    {
        list.clear();
        getObservable().notifyListeners();
    }

    @Override
    @NonNull
    public List<Score> toList()
    {
        computeAll();
        return new LinkedList<>(list);
    }

    @Nullable
    @Override
    protected Score getNewestComputed()
    {
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Nullable
    @Override
    protected Score getOldestComputed()
    {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
}
