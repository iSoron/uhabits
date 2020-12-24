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
     * Adds a checkmark to the list.
     * <p>
     * Any implementation of this method must call observable.notifyListeners()
     * after the checkmark has been added.
     *
     * @param entry the checkmark to be added.
     */
    public abstract void add(Entry entry);

    /**
     * Returns the list of checkmarks that happened within the given time
     * interval.
     * <p>
     * The list is sorted by timestamp in increasing order. That is, the first
     * element corresponds to oldest timestamp, while the last element
     * corresponds to the newest. The endpoints of the interval are included.
     *
     * @param fromTimestamp timestamp of the beginning of the interval
     * @param toTimestamp   timestamp of the end of the interval
     * @return list of checkmarks within given time interval
     */
    public abstract List<Entry> getByInterval(Timestamp fromTimestamp,
                                              Timestamp toTimestamp);

    /**
     * Returns the checkmark that has the given timestamp, or null if none
     * exists.
     *
     * @param timestamp the checkmark timestamp.
     * @return the checkmark that has the given timestamp.
     */
    @Nullable
    public abstract Entry getByTimestamp(Timestamp timestamp);

    /**
     * If a checkmark with the given timestamp exists, return its value. Otherwise, returns
     * Checkmark.NO for boolean habits and zero for numerical habits.
     */
    @NonNull
    public int getValue(Timestamp timestamp)
    {
        Entry check = getByTimestamp(timestamp);
        if (check == null) return Entry.UNKNOWN;
        return check.getValue();
    }

    @NonNull
    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns the oldest checkmark in the list.
     * <p>
     * If the list is empty, returns null. Repetitions in the future are
     * discarded.
     *
     * @return oldest checkmark in the list, or null if list is empty.
     */
    @Nullable
    public abstract Entry getOldest();

    @Nullable
    /**
     * Returns the newest checkmark in the list.
     * <p>
     * If the list is empty, returns null. Repetitions in the past are
     * discarded.
     *
     * @return newest checkmark in the list, or null if list is empty.
     */
    public abstract Entry getNewest();

    /**
     * Returns the total number of successful checkmarks for each month, from the first
     * checkmark until today, grouped by day of week.
     * <p>
     * The checkmarks are returned in a HashMap. The key is the timestamp for
     * the first day of the month, at midnight (00:00). The value is an integer
     * array with 7 entries. The first entry contains the total number of
     * successful checkmarks during the specified month that occurred on a Saturday. The
     * second entry corresponds to Sunday, and so on. If there are no
     * successful checkmarks during a certain month, the value is null.
     *
     * @return total number of checkmarks by month versus day of week
     */
    @NonNull
    public HashMap<Timestamp, Integer[]> getWeekdayFrequency()
    {
        List<Entry> entries =
                getByInterval(Timestamp.ZERO, DateUtils.getTodayWithOffset());
        HashMap<Timestamp, Integer[]> map = new HashMap<>();

        for (Entry e : entries)
        {
            if (!habit.isNumerical() && e.getValue() != Entry.YES_MANUAL)
                continue;

            Calendar date = e.getTimestamp().toCalendar();
            int weekday = e.getTimestamp().getWeekday();
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
     * Removes a given checkmark from the list.
     * <p>
     * If the list does not contain the checkmark, it is unchanged.
     * <p>
     * Any implementation of this method must call observable.notifyListeners()
     * after the checkmark has been added.
     *
     * @param entry the checkmark to be removed
     */
    public abstract void remove(@NonNull Entry entry);

    public abstract long getTotalCount();

    public void setValue(Timestamp timestamp, int value)
    {
        Entry check = getByTimestamp(timestamp);
        if (check != null) remove(check);
        add(new Entry(timestamp, value));
        habit.invalidateNewerThan(timestamp);
    }

    public abstract void removeAll();
}
