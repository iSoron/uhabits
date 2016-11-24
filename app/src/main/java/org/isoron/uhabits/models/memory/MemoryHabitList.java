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

import static org.isoron.uhabits.models.HabitList.Order.*;

/**
 * In-memory implementation of {@link HabitList}.
 */
public class MemoryHabitList extends HabitList
{
    @NonNull
    private LinkedList<Habit> list;

    private Comparator<Habit> comparator = null;

    @NonNull
    private Order order;

    public MemoryHabitList()
    {
        super();
        list = new LinkedList<>();
        order = Order.BY_POSITION;
    }

    protected MemoryHabitList(@NonNull HabitMatcher matcher)
    {
        super(matcher);
        list = new LinkedList<>();
        order = Order.BY_POSITION;
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
        resort();
    }

    @Override
    public Habit getById(long id)
    {
        for (Habit h : list)
        {
            if (h.getId() == null) continue;
            if (h.getId() == id) return h;
        }
        return null;
    }

    @NonNull
    @Override
    public Habit getByPosition(int position)
    {
        return list.get(position);
    }

    @NonNull
    @Override
    public HabitList getFiltered(HabitMatcher matcher)
    {
        MemoryHabitList habits = new MemoryHabitList(matcher);
        habits.comparator = comparator;
        for (Habit h : this) if (matcher.matches(h)) habits.add(h);
        return habits;
    }

    @Override
    public Order getOrder()
    {
        return order;
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        return list.indexOf(h);
    }

    @Override
    public Iterator<Habit> iterator()
    {
        return Collections.unmodifiableCollection(list).iterator();
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
    public void setOrder(@NonNull Order order)
    {
        this.order = order;
        this.comparator = getComparatorByOrder(order);
        resort();
    }

    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public void update(List<Habit> habits)
    {
        // NOP
    }

    private Comparator<Habit> getComparatorByOrder(Order order)
    {
        Comparator<Habit> nameComparator =
            (h1, h2) -> h1.getName().compareTo(h2.getName());

        Comparator<Habit> colorComparator = (h1, h2) -> {
            Integer c1 = h1.getColor();
            Integer c2 = h2.getColor();
            if (c1.equals(c2)) return nameComparator.compare(h1, h2);
            else return c1.compareTo(c2);
        };

        Comparator<Habit> scoreComparator = (h1, h2) -> {
            int s1 = h1.getScores().getTodayValue();
            int s2 = h2.getScores().getTodayValue();
            return Integer.compare(s2, s1);
        };

        if (order == BY_POSITION) return null;
        if (order == BY_NAME) return nameComparator;
        if (order == BY_COLOR) return colorComparator;
        if (order == BY_SCORE) return scoreComparator;
        throw new IllegalStateException();
    }

    private void resort()
    {
        if (comparator != null) Collections.sort(list, comparator);
    }
}
