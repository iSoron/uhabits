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
 * In-memory implementation of {@link HabitList}.
 */
public class MemoryHabitList extends HabitList
{
    @NonNull
    private LinkedList<Habit> list;

    public MemoryHabitList()
    {
        list = new LinkedList<>();
    }

    @Override
    public void add(@NonNull Habit habit) throws IllegalArgumentException
    {
        if (list.contains(habit))
            throw new IllegalArgumentException("habit already added");

        Long id = habit.getId();
        if (id != null && getById(id) != null)
            throw new RuntimeException("duplicate id");

        if (id == null) habit.setId((long) list.size());
        list.addLast(habit);
    }

    @Override
    public int countActive()
    {
        int count = 0;
        for (Habit h : list) if (!h.isArchived()) count++;
        return count;
    }

    @Override
    public int countWithArchived()
    {
        return list.size();
    }

    @NonNull
    @Override
    public List<Habit> getAll(boolean includeArchive)
    {
        if (includeArchive) return new LinkedList<>(list);
        return getFiltered(habit -> !habit.isArchived());
    }

    @Override
    public Habit getById(long id)
    {
        for (Habit h : list) if (h.getId() == id) return h;
        return null;
    }

    @Nullable
    @Override
    public Habit getByPosition(int position)
    {
        return list.get(position);
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        return list.indexOf(h);
    }

    @Override
    public void remove(@NonNull Habit habit)
    {
        list.remove(habit);
    }

    @Override
    public void reorder(Habit from, Habit to)
    {
        int toPos = indexOf(to);
        list.remove(from);
        list.add(toPos, from);
    }

    @Override
    public void update(List<Habit> habits)
    {
        // NOP
    }
}
