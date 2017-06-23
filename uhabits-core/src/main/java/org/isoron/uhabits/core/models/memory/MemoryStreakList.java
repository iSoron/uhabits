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

public class MemoryStreakList extends StreakList
{
    ArrayList<Streak> list;

    public MemoryStreakList(Habit habit)
    {
        super(habit);
        list = new ArrayList<>();
    }

    @Override
    public Streak getNewestComputed()
    {
        Streak newest = null;

        for (Streak s : list)
            if (newest == null || s.getEnd() > newest.getEnd()) newest = s;

        return newest;
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        list.clear();
        observable.notifyListeners();
    }

    @Override
    protected void add(@NonNull List<Streak> streaks)
    {
        list.addAll(streaks);
        Collections.sort(list, (s1, s2) -> s2.compareNewer(s1));
        observable.notifyListeners();
    }

    @Override
    protected void removeNewestComputed()
    {
        Streak newest = getNewestComputed();
        if (newest != null) list.remove(newest);
    }

    @Override
    public List<Streak> getAll()
    {
        rebuild();
        return new LinkedList<>(list);
    }
}
