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

import org.apache.commons.lang3.builder.*;
import org.isoron.uhabits.core.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.annotation.concurrent.*;

import static org.isoron.uhabits.core.models.Entry.*;
import static org.isoron.uhabits.core.utils.StringUtils.*;

/**
 * The collection of {@link Entry}s belonging to a habit.
 */
@ThreadSafe
public class EntryList
{
    protected final Habit habit;

    protected ArrayList<Entry> list;

    public EntryList(Habit habit)
    {
        this.habit = habit;
        this.list = new ArrayList<>();
    }

    @NonNull
    static List<Entry> buildEntriesFromInterval(Entry[] original,
                                                ArrayList<Interval> intervals)
    {
        if (original.length == 0) throw new IllegalArgumentException();

        Timestamp today = DateUtils.getTodayWithOffset();
        Timestamp begin = original[0].getTimestamp();
        if (intervals.size() > 0) begin = Timestamp.oldest(begin, intervals.get(0).begin);

        int nDays = begin.daysUntil(today) + 1;
        List<Entry> entries = new ArrayList<>(nDays);
        for (int i = 0; i < nDays; i++)
            entries.add(new Entry(today.minus(i), UNKNOWN));

        for (Interval interval : intervals)
        {
            for (int i = 0; i < interval.length(); i++)
            {
                Timestamp date = interval.begin.plus(i);
                int offset = date.daysUntil(today);
                if (offset < 0) continue;
                entries.set(offset, new Entry(date, YES_AUTO));
            }
        }

        for (Entry e : original)
        {
            Timestamp date = e.getTimestamp();
            int offset = date.daysUntil(today);
            int value = e.getValue();
            int prevValue = entries.get(offset).getValue();
            if (prevValue < value)
                entries.set(offset, new Entry(date, value));
        }

        return entries;
    }

    /**
     * For non-daily habits, some manual entries generate many
     * automatic entries. For example, for weekly habits, each repetition generates
     * seven checkmarks. For twice-a-week habits, two repetitions that are close
     * enough together also generate seven checkmarks.
     * <p>
     * This group of generated entries is represented by an interval. This function
     * computes the list of intervals for a given list of original entries. It tries
     * to build the intervals as far away in the future as possible.
     */
    @NonNull
    static ArrayList<Interval> buildIntervals(@NonNull Frequency freq,
                                              @NonNull Entry[] entries)
    {
        ArrayList<Entry> filteredEntries = new ArrayList<>();
        for (Entry e : entries)
            if (e.getValue() == YES_MANUAL)
                filteredEntries.add(e);

        int num = freq.getNumerator();
        int den = freq.getDenominator();

        ArrayList<Interval> intervals = new ArrayList<>();
        for (int i = 0; i < filteredEntries.size() - num + 1; i++)
        {
            Entry first = filteredEntries.get(i);
            Entry last = filteredEntries.get(i + num - 1);

            long distance = first.getTimestamp().daysUntil(last.getTimestamp());
            if (distance >= den) continue;

            Timestamp begin = first.getTimestamp();
            Timestamp center = last.getTimestamp();
            Timestamp end = begin.plus(den - 1);
            intervals.add(new Interval(begin, center, end));
        }

        return intervals;
    }

    /**
     * Starting from the second newest interval, this function tries to slide the
     * intervals backwards into the past, so that gaps are eliminated and
     * streaks are maximized.
     */
    static void snapIntervalsTogether(@NonNull ArrayList<Interval> intervals)
    {
        int n = intervals.size();
        for (int i = n - 2; i >= 0; i--)
        {
            Interval curr = intervals.get(i);
            Interval next = intervals.get(i + 1);

            int gapNextToCurrent = next.begin.daysUntil(curr.end);
            int gapCenterToEnd = curr.center.daysUntil(curr.end);

            if (gapNextToCurrent >= 0)
            {
                int shift = Math.min(gapCenterToEnd, gapNextToCurrent + 1);
                intervals.set(i, new Interval(curr.begin.minus(shift),
                        curr.center,
                        curr.end.minus(shift)));
            }
        }
    }

    public void add(List<Entry> entries)
    {
        list.addAll(entries);
        Collections.sort(list,
                (c1, c2) -> c2.getTimestamp().compare(c1.getTimestamp()));

    }

    /**
     * Returns the values for all the checkmarks, since the oldest repetition of
     * the habit until today.
     * <p>
     * If there are no repetitions at all, returns an empty array. The values
     * are returned in an array containing one integer value for each day since
     * the first repetition of the habit until today. The first entry
     * corresponds to today, the second entry corresponds to yesterday, and so
     * on.
     *
     * @return values for the checkmarks in the interval
     */
    @NonNull
    public synchronized final int[] getAllValues()
    {
        Entry oldestOriginal = habit.getOriginalEntries().getOldest();
        if (oldestOriginal == null) return new int[0];

        Timestamp fromTimestamp = oldestOriginal.getTimestamp();
        Timestamp toTimestamp = DateUtils.getTodayWithOffset();

        return getValues(fromTimestamp, toTimestamp);
    }

    /**
     * Returns the list of checkmarks that fall within the given interval.
     * <p>
     * There is exactly one checkmark per day in the interval. The endpoints of
     * the interval are included. The list is ordered by timestamp (decreasing).
     * That is, the first checkmark corresponds to the newest timestamp, and the
     * last checkmark corresponds to the oldest timestamp.
     *
     * @param from timestamp of the beginning of the interval.
     * @param to   timestamp of the end of the interval.
     * @return the list of checkmarks within the interval.
     */
    @NonNull
    public List<Entry> getByInterval(Timestamp from,
                                     Timestamp to)
    {
        compute();

        Timestamp newestComputed = new Timestamp(0);
        Timestamp oldestComputed = new Timestamp(0).plus(1000000);

        Entry newest = getNewestComputed();
        Entry oldest = getOldestComputed();
        if (newest != null) newestComputed = newest.getTimestamp();
        if (oldest != null) oldestComputed = oldest.getTimestamp();

        List<Entry> filtered = new ArrayList<>(
                Math.max(0, oldestComputed.daysUntil(newestComputed) + 1));

        for (int i = 0; i <= from.daysUntil(to); i++)
        {
            Timestamp t = to.minus(i);
            if (t.isNewerThan(newestComputed) || t.isOlderThan(oldestComputed))
                filtered.add(new Entry(t, Entry.UNKNOWN));
            else
                filtered.add(list.get(t.daysUntil(newestComputed)));
        }

        return filtered;

    }

    /**
     * Returns the checkmark for today.
     *
     * @return checkmark for today
     */
    @Nullable
    public synchronized final Entry getToday()
    {
        compute();
        Timestamp today = DateUtils.getTodayWithOffset();
        return getByInterval(today, today).get(0);
    }

    /**
     * Returns the value of today's checkmark.
     *
     * @return value of today's checkmark
     */
    public synchronized int getTodayValue()
    {
        Entry today = getToday();
        if (today != null) return today.getValue();
        else return UNKNOWN;
    }

    public synchronized int getThisWeekValue(int firstWeekday)
    {
        return getThisIntervalValue(DateUtils.TruncateField.WEEK_NUMBER, firstWeekday);
    }

    public synchronized int getThisMonthValue()
    {
        return getThisIntervalValue(DateUtils.TruncateField.MONTH, Calendar.SATURDAY);
    }


    public synchronized int getThisQuarterValue()
    {
        return getThisIntervalValue(DateUtils.TruncateField.QUARTER, Calendar.SATURDAY);
    }


    public synchronized int getThisYearValue()
    {
        return getThisIntervalValue(DateUtils.TruncateField.YEAR, Calendar.SATURDAY);
    }

    private int getThisIntervalValue(DateUtils.TruncateField truncateField, int firstWeekday)
    {
        List<Entry> groups = habit.getComputedEntries().groupBy(truncateField, firstWeekday, 1);
        if (groups.isEmpty()) return 0;
        return groups.get(0).getValue();
    }

    /**
     * Returns the values of the checkmarks that fall inside a certain interval
     * of time.
     * <p>
     * The values are returned in an array containing one integer value for each
     * day of the interval. The first entry corresponds to the most recent day
     * in the interval. Each subsequent entry corresponds to one day older than
     * the previous entry. The boundaries of the time interval are included.
     *
     * @param from timestamp for the oldest checkmark
     * @param to   timestamp for the newest checkmark
     * @return values for the checkmarks inside the given interval
     */
    public final int[] getValues(Timestamp from, Timestamp to)
    {
        if (from.isNewerThan(to)) return new int[0];

        List<Entry> entries = getByInterval(from, to);
        int values[] = new int[entries.size()];

        int i = 0;
        for (Entry c : entries)
            values[i++] = c.getValue();

        return values;
    }

    /**
     * Marks as invalid every checkmark that has timestamp either equal or newer
     * than a given timestamp. These checkmarks will be recomputed at the next
     * time they are queried.
     *
     * @param timestamp the timestamp
     */
    public void invalidateNewerThan(Timestamp timestamp)
    {
        list.clear();
    }

    /**
     * Writes the entire list of checkmarks to the given writer, in CSV format.
     *
     * @param out the writer where the CSV will be output
     * @throws IOException in case write operations fail
     */
    public final void writeCSV(Writer out) throws IOException
    {
        int values[];

        synchronized (this)
        {
            compute();
            values = getAllValues();
        }

        Timestamp timestamp = DateUtils.getToday();
        SimpleDateFormat dateFormat = DateFormats.getCSVDateFormat();

        for (int value : values)
        {
            String date = dateFormat.format(timestamp.toJavaDate());
            out.write(String.format("%s,%d\n", date, value));
            timestamp = timestamp.minus(1);
        }
    }

    /**
     * Computes and stores one checkmark for each day, from the first habit
     * repetition to today. If this list is already computed, does nothing.
     */
    protected final synchronized void compute()
    {
        final Timestamp today = DateUtils.getTodayWithOffset();

        Entry newest = getNewestComputed();
        if (newest != null && newest.getTimestamp().equals(today)) return;
        invalidateNewerThan(Timestamp.ZERO);

        Entry oldestRep = habit.getOriginalEntries().getOldest();
        if (oldestRep == null) return;
        final Timestamp from = oldestRep.getTimestamp();

        if (from.isNewerThan(today)) return;

        Entry reps[] = habit
                .getOriginalEntries()
                .getByInterval(from, today)
                .toArray(new Entry[0]);

        if (habit.isNumerical()) computeNumerical(reps);
        else computeYesNo(reps);
    }

    @Nullable
    protected Entry getNewestComputed()
    {
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Nullable
    protected Entry getOldestComputed()
    {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    private void computeNumerical(Entry[] original)
    {
        if (original.length == 0) return;

        Timestamp today = DateUtils.getTodayWithOffset();
        Timestamp begin = original[0].getTimestamp();

        int nDays = begin.daysUntil(today) + 1;
        List<Entry> computed = new ArrayList<>(nDays);
        for (int i = 0; i < nDays; i++)
            computed.add(new Entry(today.minus(i), 0));

        for (Entry e : original)
        {
            int offset = e.getTimestamp().daysUntil(today);
            computed.set(offset, new Entry(e.getTimestamp(), e.getValue()));
        }

        add(computed);
    }

    private void computeYesNo(Entry[] original)
    {
        ArrayList<Interval> intervals;
        intervals = buildIntervals(habit.getFrequency(), original);
        snapIntervalsTogether(intervals);
        add(buildEntriesFromInterval(original, intervals));
    }

    public List<Entry> getAll()
    {
        Entry oldest = habit.getOriginalEntries().getOldest();
        if (oldest == null) return new ArrayList<>();
        return getByInterval(oldest.getTimestamp(), DateUtils.getTodayWithOffset());
    }

    static final class Interval
    {
        final Timestamp begin;

        final Timestamp center;

        final Timestamp end;

        Interval(Timestamp begin, Timestamp center, Timestamp end)
        {
            this.begin = begin;
            this.center = center;
            this.end = end;
        }

        public int length()
        {
            return begin.daysUntil(end) + 1;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Interval interval = (Interval) o;

            return new EqualsBuilder()
                    .append(begin, interval.begin)
                    .append(center, interval.center)
                    .append(end, interval.end)
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 37)
                    .append(begin)
                    .append(center)
                    .append(end)
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, defaultToStringStyle())
                    .append("begin", begin)
                    .append("center", center)
                    .append("end", end)
                    .toString();
        }
    }

    @NonNull
    public List<Entry> groupBy(DateUtils.TruncateField field, int firstWeekday)
    {
        return groupBy(field, firstWeekday, 0);
    }


    @NonNull
    public List<Entry> groupBy(DateUtils.TruncateField field,
                               int firstWeekday,
                               int maxGroups)
    {
        List<Entry> checks = getAll();

        int count = 0;
        Timestamp[] truncatedTimestamps = new Timestamp[checks.size()];
        int[] values = new int[checks.size()];

        for (Entry rep : checks)
        {
            Timestamp tt = rep.getTimestamp().truncate(field, firstWeekday);
            if (count == 0 || !truncatedTimestamps[count - 1].equals(tt))
            {
                if (maxGroups > 0 && count >= maxGroups) break;
                truncatedTimestamps[count++] = tt;
            }

            if (habit.isNumerical())
                values[count - 1] += rep.getValue();
            else if (rep.getValue() == Entry.YES_MANUAL)
                values[count - 1] += 1000;

        }

        ArrayList<Entry> groupedEntries = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            Entry rep = new Entry(truncatedTimestamps[i], values[i]);
            groupedEntries.add(rep);
        }

        return groupedEntries;
    }
}
