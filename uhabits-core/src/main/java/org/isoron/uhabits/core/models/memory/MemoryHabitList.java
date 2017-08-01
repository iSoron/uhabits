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

import static org.isoron.uhabits.core.models.HabitList.Order.*;

/**
 * In-memory implementation of {@link HabitList}.
 */
public class MemoryHabitList extends HabitList
{
    @NonNull
    private LinkedList<Habit> list = new LinkedList<>();

    private Comparator<Habit> comparator = null;

    @NonNull
    private Order order = Order.BY_POSITION;

    @Nullable
    private MemoryHabitList parent = null;

    public MemoryHabitList()
    {
        super();
    }

    protected MemoryHabitList(@NonNull HabitMatcher matcher,
                              Comparator<Habit> comparator,
                              @NonNull MemoryHabitList parent)
    {
        super(matcher);
        this.parent = parent;
        this.comparator = comparator;
        parent.getObservable().addListener(this::loadFromParent);
        loadFromParent();
    }

    @Override
    public synchronized void add(@NonNull Habit habit)
        throws IllegalArgumentException
    {
        throwIfHasParent();
        if (list.contains(habit))
            throw new IllegalArgumentException("habit already added");

        Long id = habit.getId();
        if (id != null && getById(id) != null)
            throw new RuntimeException("duplicate id");

        if (id == null) habit.setId((long) list.size());
        list.addLast(habit);
        resort();

        getObservable().notifyListeners();
    }

    @Override
    public synchronized Habit getById(long id)
    {
        for (Habit h : list)
        {
            if (h.getId() == null) throw new IllegalStateException();
            if (h.getId() == id) return h;
        }
        return null;
    }

    @NonNull
    @Override
    public synchronized Habit getByPosition(int position)
    {
        return list.get(position);
    }

    @NonNull
    @Override
    public synchronized HabitList getFiltered(HabitMatcher matcher)
    {
        return new MemoryHabitList(matcher, comparator, this);
    }

    @Override
    public synchronized Order getOrder()
    {
        return order;
    }

    @Override
    public synchronized void setOrder(@NonNull Order order)
    {
        this.order = order;
        this.comparator = getComparatorByOrder(order);
        resort();
        getObservable().notifyListeners();
    }

    private Comparator<Habit> getComparatorByOrder(Order order)
    {
        Comparator<Habit> nameComparator =
            (h1, h2) -> h1.getName().compareTo(h2.getName());

        Comparator<Habit> colorComparator = (h1, h2) ->
        {
            Integer c1 = h1.getColor();
            Integer c2 = h2.getColor();
            if (c1.equals(c2)) return nameComparator.compare(h1, h2);
            else return c1.compareTo(c2);
        };

        Comparator<Habit> scoreComparator = (h1, h2) ->
        {
            Double s1 = h1.getScores().getTodayValue();
            Double s2 = h2.getScores().getTodayValue();
            if (s1.equals(s2)) return nameComparator.compare(h1, h2);
            else return s2.compareTo(s1);
        };

        Comparator<Habit> positionComparator = (h1, h2) ->
        {
            Integer p1 = h1.getPosition();
            Integer p2 = h2.getPosition();
            if (p1.equals(p2)) return nameComparator.compare(h1, h2);
            else return p1.compareTo(p2);
        };

        if (order == BY_POSITION) return positionComparator;
        if (order == BY_NAME) return nameComparator;
        if (order == BY_COLOR) return colorComparator;
        if (order == BY_SCORE) return scoreComparator;
        throw new IllegalStateException();
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        return list.indexOf(h);
    }

    @NonNull
    @Override
    public Iterator<Habit> iterator()
    {
        return Collections.unmodifiableCollection(list).iterator();
    }

    @Override
    public synchronized void remove(@NonNull Habit habit)
    {
        throwIfHasParent();
        list.remove(habit);
        getObservable().notifyListeners();
    }

    @Override
    public synchronized void reorder(@NonNull Habit from, @NonNull Habit to)
    {
        throwIfHasParent();
        if (order != BY_POSITION) throw new IllegalStateException(
            "cannot reorder automatically sorted list");

        if (indexOf(from) < 0) throw new IllegalArgumentException(
            "list does not contain (from) habit");

        int toPos = indexOf(to);
        if (toPos < 0) throw new IllegalArgumentException(
            "list does not contain (to) habit");

        list.remove(from);
        list.add(toPos, from);

        int position = 0;
        for(Habit h : list)
            h.setPosition(position++);

        getObservable().notifyListeners();
    }

    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public void update(List<Habit> habits)
    {
        resort();
        getObservable().notifyListeners();
    }

    private void throwIfHasParent()
    {
        if (parent != null) throw new IllegalStateException(
            "Filtered lists cannot be modified directly. " +
            "You should modify the parent list instead.");
    }

    private synchronized void loadFromParent()
    {
        if (parent == null) throw new IllegalStateException();

        list.clear();
        for (Habit h : parent) if (filter.matches(h)) list.add(h);
        resort();
    }

    private synchronized void resort()
    {
        if (comparator != null) Collections.sort(list, comparator);
    }
}
