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

import static org.isoron.uhabits.core.models.HabitList.Order.*;

/**
 * In-memory implementation of {@link HabitList}.
 */
public class MemoryHabitList extends HabitList
{
    @NonNull
    private LinkedList<Habit> list = new LinkedList<>();

    @NonNull
    private Order primaryOrder = Order.BY_POSITION;

    @NonNull
    private Order secondaryOrder = Order.BY_NAME_ASC;

    private Comparator<Habit> comparator = getComposedComparatorByOrder(primaryOrder, secondaryOrder);

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
        this.primaryOrder = parent.primaryOrder;
        this.secondaryOrder = parent.secondaryOrder;
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

    @Override
    public synchronized Habit getByUUID(String uuid)
    {
        for (Habit h : list) if (h.getUUID().equals(uuid)) return h;
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
    public synchronized Order getPrimaryOrder()
    {
        return primaryOrder;
    }

    @Override
    public synchronized Order getSecondaryOrder()
    {
        return secondaryOrder;
    }

    @Override
    public synchronized void setPrimaryOrder(@NonNull Order order)
    {
        this.primaryOrder = order;
        this.comparator = getComposedComparatorByOrder(this.primaryOrder, this.secondaryOrder);
        resort();
    }

    @Override
    public void setSecondaryOrder(@NonNull Order order)
    {
        this.secondaryOrder = order;
        this.comparator = getComposedComparatorByOrder(this.primaryOrder, this.secondaryOrder);
        resort();
    }

    private Comparator<Habit> getComposedComparatorByOrder(Order firstOrder, Order secondOrder)
    {
        return (h1, h2) -> {
            int firstResult = getComparatorByOrder(firstOrder).compare(h1, h2);

            if (firstResult != 0 || secondOrder == null) {
                return firstResult;
            }

            return getComparatorByOrder(secondOrder).compare(h1, h2);
        };
    }

    private Comparator<Habit> getComparatorByOrder(Order order) {
        Comparator<Habit> nameComparatorAsc = (h1, h2) ->
        h1.getName().compareTo(h2.getName());

        Comparator<Habit> nameComparatorDesc = (h1, h2) ->
                nameComparatorAsc.compare(h2, h1);

        Comparator<Habit> colorComparatorAsc = (h1, h2) ->
                h1.getColor().compareTo(h2.getColor());

        Comparator<Habit> colorComparatorDesc = (h1, h2) ->
                colorComparatorAsc.compare(h2, h1);

        Comparator<Habit> scoreComparatorDesc = (h1, h2) ->
                Double.compare(h1.getScores().getTodayValue(), h2.getScores().getTodayValue());

        Comparator<Habit> scoreComparatorAsc = (h1, h2) ->
                scoreComparatorDesc.compare(h2, h1);

        Comparator<Habit> positionComparator = (h1, h2) ->
                h1.getPosition().compareTo(h2.getPosition());

        Comparator<Habit> statusComparatorDesc = (h1, h2) ->
        {
            if (h1.isCompletedToday() != h2.isCompletedToday()) {
                return h1.isCompletedToday() ? -1 : 1;
            }

            if (h1.isNumerical() != h2.isNumerical()) {
                return h1.isNumerical() ? -1 : 1;
            }

            Integer v1 = Objects.requireNonNull(h1.getCheckmarks().getToday()).getValue();
            Integer v2 = Objects.requireNonNull(h2.getCheckmarks().getToday()).getValue();

            return v2.compareTo(v1);
        };

        Comparator<Habit> statusComparatorAsc = (h1, h2) -> statusComparatorDesc.compare(h2, h1);

        if (order == BY_POSITION) return positionComparator;
        if (order == BY_NAME_ASC) return nameComparatorAsc;
        if (order == BY_NAME_DESC) return nameComparatorDesc;
        if (order == BY_COLOR_ASC) return colorComparatorAsc;
        if (order == BY_COLOR_DESC) return colorComparatorDesc;
        if (order == BY_SCORE_DESC) return scoreComparatorDesc;
        if (order == BY_SCORE_ASC) return scoreComparatorAsc;
        if (order == BY_STATUS_DESC) return statusComparatorDesc;
        if (order == BY_STATUS_ASC) return statusComparatorAsc;
        throw new IllegalStateException();
    }

    @Override
    public synchronized int indexOf(@NonNull Habit h)
    {
        return list.indexOf(h);
    }

    @NonNull
    @Override
    public synchronized Iterator<Habit> iterator()
    {
        return new ArrayList<>(list).iterator();
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
        if (primaryOrder != BY_POSITION) throw new IllegalStateException(
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
    public synchronized int size()
    {
        return list.size();
    }

    @Override
    public synchronized void update(List<Habit> habits)
    {
        resort();
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

    public synchronized void resort()
    {
        if (comparator != null) Collections.sort(list, comparator);
        getObservable().notifyListeners();
    }
}
