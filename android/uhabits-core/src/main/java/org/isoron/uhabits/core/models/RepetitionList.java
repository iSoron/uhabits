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

package org.isoron.uhabits.core.models;

import androidx.annotation.*;

import org.isoron.uhabits.core.utils.*;

import java.util.*;

/**
 * The collection of {@link Repetition}s belonging to a habit.
 */
public abstract class RepetitionList
{
    @NonNull
    protected final Habit habit;

    @NonNull
    protected final ModelObservable observable;

    public RepetitionList(@NonNull Habit habit)
    {
        this.habit = habit;
        this.observable = new ModelObservable();
    }

    /**
     * Adds a repetition to the list.
     * <p>
     * Any implementation of this method must call observable.notifyListeners()
     * after the repetition has been added.
     *
     * @param repetition the repetition to be added.
     */
    public abstract void add(Repetition repetition);

    /**
     * Returns true if the list contains a repetition that has the given
     * timestamp.
     *
     * @param timestamp the timestamp to find.
     * @return true if list contains repetition with given timestamp, false
     * otherwise.
     */
    public boolean containsTimestamp(Timestamp timestamp)
    {
        return (getByTimestamp(timestamp) != null);
    }

    /**
     * Returns the list of repetitions that happened within the given time
     * interval.
     * <p>
     * The list is sorted by timestamp in increasing order. That is, the first
     * element corresponds to oldest timestamp, while the last element
     * corresponds to the newest. The endpoints of the interval are included.
     *
     * @param fromTimestamp timestamp of the beginning of the interval
     * @param toTimestamp   timestamp of the end of the interval
     * @return list of repetitions within given time interval
     */
    public abstract List<Repetition> getByInterval(Timestamp fromTimestamp,
                                                   Timestamp toTimestamp);

    /**
     * Returns the repetition that has the given timestamp, or null if none
     * exists.
     *
     * @param timestamp the repetition timestamp.
     * @return the repetition that has the given timestamp.
     */
    @Nullable
    public abstract Repetition getByTimestamp(Timestamp timestamp);

    @NonNull
    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns the oldest repetition in the list.
     * <p>
     * If the list is empty, returns null. Repetitions in the future are
     * discarded.
     *
     * @return oldest repetition in the list, or null if list is empty.
     */
    @Nullable
    public abstract Repetition getOldest();

    @Nullable
    /**
     * Returns the newest repetition in the list.
     * <p>
     * If the list is empty, returns null. Repetitions in the past are
     * discarded.
     *
     * @return newest repetition in the list, or null if list is empty.
     */
    public abstract Repetition getNewest();

    /**
     * Returns the total number of successful repetitions for each month, from the first
     * repetition until today, grouped by day of week.
     * <p>
     * The repetitions are returned in a HashMap. The key is the timestamp for
     * the first day of the month, at midnight (00:00). The value is an integer
     * array with 7 entries. The first entry contains the total number of
     * successful repetitions during the specified month that occurred on a Saturday. The
     * second entry corresponds to Sunday, and so on. If there are no
     * successful repetitions during a certain month, the value is null.
     *
     * @return total number of repetitions by month versus day of week
     */
    @NonNull
    public HashMap<Timestamp, Integer[]> getWeekdayFrequency()
    {
        List<Repetition> reps =
                getByInterval(Timestamp.ZERO, DateUtils.getToday());
        HashMap<Timestamp, Integer[]> map = new HashMap<>();

        for (Repetition r : reps)
        {
            if ((habit.getData().type == Habit.YES_NO_HABIT)
                    && (r.getValue() == Checkmark.SKIPPED_EXPLICITLY)) {
                continue;
            }

            Calendar date = r.getTimestamp().toCalendar();
            int weekday = r.getTimestamp().getWeekday();
            date.set(Calendar.DAY_OF_MONTH, 1);

            Timestamp timestamp = new Timestamp(date.getTimeInMillis());
            Integer[] list = map.get(timestamp);

            if (list == null)
            {
                list = new Integer[7];
                Arrays.fill(list, 0);
                map.put(timestamp, list);
            }

            list[weekday]++;
        }

        return map;
    }

    /**
     * Removes a given repetition from the list.
     * <p>
     * If the list does not contain the repetition, it is unchanged.
     * <p>
     * Any implementation of this method must call observable.notifyListeners()
     * after the repetition has been added.
     *
     * @param repetition the repetition to be removed
     */
    public abstract void remove(@NonNull Repetition repetition);

    /**
     * Adds or remove a repetition at a certain timestamp.
     * <p>
     * If there exists a repetition on the list with the given timestamp, the
     * method removes this repetition from the list and returns it. If there are
     * no repetitions with the given timestamp, creates and adds one to the
     * list, then returns it.
     *
     * @param timestamp the timestamp for the timestamp that should be added or
     *                  removed.
     * @return the repetition that has been added or removed.
     */
    @NonNull
    public synchronized Repetition toggle(Timestamp timestamp)
    {
        if (habit.isNumerical())
            throw new IllegalStateException("habit must NOT be numerical");

        Repetition rep = getByTimestamp(timestamp);
        if (rep != null) {
            remove(rep);
            if (rep.getValue() == Checkmark.CHECKED_EXPLICITLY) {
                rep = new Repetition(timestamp, Checkmark.SKIPPED_EXPLICITLY);
                add(rep);
            }
        }
        else
        {
            rep = new Repetition(timestamp, Checkmark.CHECKED_EXPLICITLY);
            add(rep);
        }

        habit.invalidateNewerThan(timestamp);
        return rep;
    }

    /**
     * Returns the number of all successful repetitions
     *
     * @return number of all successful repetitions
     */
    @NonNull
    public abstract long getTotalSuccessfulCount();

    public void toggle(Timestamp timestamp, int value)
    {
        Repetition rep = getByTimestamp(timestamp);
        if (rep != null) remove(rep);
        add(new Repetition(timestamp, value));
        habit.invalidateNewerThan(timestamp);
    }

    public abstract void removeAll();
}
