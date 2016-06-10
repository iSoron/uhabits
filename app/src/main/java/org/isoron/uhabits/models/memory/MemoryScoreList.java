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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.models.ScoreList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MemoryScoreList extends ScoreList
{
    List<Score> list;

    public MemoryScoreList(Habit habit)
    {
        super(habit);
        list = new LinkedList<>();
    }

    @Override
    public int getValue(long timestamp)
    {
        Score s = get(timestamp);
        if (s != null) return s.getValue();
        return 0;
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        List<Score> discard = new LinkedList<>();

        for (Score s : list)
            if (s.getTimestamp() >= timestamp) discard.add(s);

        list.removeAll(discard);
    }

    @Override
    @NonNull
    public List<Score> getAll()
    {
        computeAll();
        return new LinkedList<>(list);
    }

    @Override
    protected void add(List<Score> scores)
    {
        list.addAll(scores);
        Collections.sort(list,
            (s1, s2) -> Long.signum(s2.getTimestamp() - s1.getTimestamp()));
    }

    @Override
    @Nullable
    protected Score get(long timestamp)
    {
        computeAll();
        for (Score s : list)
            if (s.getTimestamp() == timestamp) return s;

        return null;
    }

    @Nullable
    @Override
    protected Score getNewestComputed()
    {
        if(list.isEmpty()) return null;
        return list.get(0);
    }
}
