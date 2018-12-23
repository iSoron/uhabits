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

import android.support.annotation.*;

import org.apache.commons.lang3.builder.*;
import org.isoron.uhabits.core.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.annotation.concurrent.*;

import static org.isoron.uhabits.core.models.Checkmark.*;
import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;

/**
 * The collection of {@link Checkmark}s belonging to a habit.
 */
@ThreadSafe
public abstract class CheckmarkList
{
    protected final Habit habit;

    public final ModelObservable observable;

    public CheckmarkList(Habit habit)
    {
        this.habit = habit;
        this.observable = new ModelObservable();
    }

    @NonNull
    static List<Checkmark> buildCheckmarksFromIntervals(Repetition[] reps,
                                                        ArrayList<Interval> intervals)
    {
        if (reps.length == 0) throw new IllegalArgumentException();

        Timestamp today = DateUtils.getToday();
        Timestamp begin = reps[0].getTimestamp();
        if (intervals.size() > 0) begin = Timestamp.oldest(begin, intervals.get(0).begin);

        int nDays = begin.daysUntil(today) + 1;
        List<Checkmark> checkmarks = new ArrayList<>(nDays);
        for (int i = 0; i < nDays; i++)
            checkmarks.add(new Checkmark(today.minus(i), UNCHECKED));

        for (Interval interval : intervals)
        {
            for (int i = 0; i < interval.length(); i++)
            {
                Timestamp date = interval.begin.plus(i);
                int offset = date.daysUntil(today);
                if (offset < 0) continue;
                checkmarks.set(offset, new Checkmark(date, CHECKED_IMPLICITLY));
            }
        }

        for (Repetition rep : reps)
        {
            Timestamp date = rep.getTimestamp();
            int offset = date.daysUntil(today);
            checkmarks.set(offset, new Checkmark(date, CHECKED_EXPLICITLY));
        }

        return checkmarks;
    }

    /**
     * For non-daily habits, some groups of repetitions generate many
     * checkmarks. For example, for weekly habits, each repetition generates
     * seven checkmarks. For twice-a-week habits, two repetitions that are close
     * enough together also generate seven checkmarks.
     * <p>
     * This group of generated checkmarks, for a given set of repetition, is
     * represented by an interval. This function computes the list of intervals
     * for a given list of repetitions. It tries to build the intervals as far
     * away in the future as possible.
     */
    @NonNull
    static ArrayList<Interval> buildIntervals(@NonNull Frequency freq,
                                              @NonNull Repetition[] reps)
    {
        int num = freq.getNumerator();
        int den = freq.getDenominator();

        ArrayList<Interval> intervals = new ArrayList<>();
        for (int i = 0; i < reps.length - num + 1; i++)
        {
            Repetition first = reps[i];
            Repetition last = reps[i + num - 1];

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
     * Starting from the oldest interval, this function tries to slide the
     * intervals backwards into the past, so that gaps are eliminated and
     * streaks are maximized. When it detects that sliding an interval
     * would not help fixing any gap, it leaves the interval unchanged.
     */
    static void snapIntervalsTogether(@NonNull ArrayList<Interval> intervals)
    {
        for (int i = 1; i < intervals.size(); i++)
        {
            Interval curr = intervals.get(i);
            Interval prev = intervals.get(i - 1);

            int gap = prev.end.daysUntil(curr.begin) - 1;
            if (gap <= 0 || curr.end.minus(gap).isOlderThan(curr.center)) continue;
            intervals.set(i, new Interval(curr.begin.minus(gap), curr.center,
                curr.end.minus(gap)));
        }
    }

    /**
     * Adds all the given checkmarks to the list.
     * <p>
     * This should never be called by the application, since the checkmarks are
     * computed automatically from the list of repetitions.
     *
     * @param checkmarks the checkmarks to be added.
     */
    public abstract void add(List<Checkmark> checkmarks);

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
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return new int[0];

        Timestamp fromTimestamp = oldestRep.getTimestamp();
        Timestamp toTimestamp = DateUtils.getToday();

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
     * @param fromTimestamp timestamp of the beginning of the interval.
     * @param toTimestamp   timestamp of the end of the interval.
     * @return the list of checkmarks within the interval.
     */
    @NonNull
    public abstract List<Checkmark> getByInterval(Timestamp fromTimestamp,
                                                  Timestamp toTimestamp);

    /**
     * Returns the checkmark for today.
     *
     * @return checkmark for today
     */
    @Nullable
    public synchronized final Checkmark getToday()
    {
        compute();
        Timestamp today = DateUtils.getToday();
        return getByInterval(today, today).get(0);
    }

    /**
     * Returns the value of today's checkmark.
     *
     * @return value of today's checkmark
     */
    public synchronized int getTodayValue()
    {
        Checkmark today = getToday();
        if (today != null) return today.getValue();
        else return UNCHECKED;
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

        List<Checkmark> checkmarks = getByInterval(from, to);
        int values[] = new int[checkmarks.size()];

        int i = 0;
        for (Checkmark c : checkmarks)
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
    public abstract void invalidateNewerThan(Timestamp timestamp);

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
        final Timestamp today = DateUtils.getToday();

        Checkmark newest = getNewestComputed();
        if (newest != null && newest.getTimestamp().equals(today)) return;
        invalidateNewerThan(Timestamp.ZERO);

        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return;
        final Timestamp from = oldestRep.getTimestamp();

        Repetition reps[] = habit
            .getRepetitions()
            .getByInterval(from, today)
            .toArray(new Repetition[0]);

        if (habit.isNumerical()) computeNumerical(reps);
        else computeYesNo(reps);
    }

    /**
     * Returns newest checkmark that has already been computed.
     *
     * @return newest checkmark already computed
     */
    @Nullable
    protected abstract Checkmark getNewestComputed();

    /**
     * Returns oldest checkmark that has already been computed.
     *
     * @return oldest checkmark already computed
     */
    @Nullable
    protected abstract Checkmark getOldestComputed();

    private void computeNumerical(Repetition[] reps)
    {
        if (reps.length == 0) throw new IllegalArgumentException();

        Timestamp today = DateUtils.getToday();
        Timestamp begin = reps[0].getTimestamp();

        int nDays = begin.daysUntil(today) + 1;
        List<Checkmark> checkmarks = new ArrayList<>(nDays);
        for (int i = 0; i < nDays; i++)
            checkmarks.add(new Checkmark(today.minus(i), 0));

        for (Repetition rep : reps)
        {
            int offset = rep.getTimestamp().daysUntil(today);
            checkmarks.set(offset, new Checkmark(rep.getTimestamp(), rep.getValue()));
        }

        add(checkmarks);
    }

    private void computeYesNo(Repetition[] reps)
    {
        ArrayList<Interval> intervals;
        intervals = buildIntervals(habit.getFrequency(), reps);
        snapIntervalsTogether(intervals);
        add(buildCheckmarksFromIntervals(reps, intervals));
    }

    public List<Checkmark> getAll() {
        Repetition oldest = habit.getRepetitions().getOldest();
        if(oldest == null) return new ArrayList<>();
        return getByInterval(oldest.getTimestamp(), DateUtils.getToday());
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

        public int length() {
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
    public List<Checkmark> groupBy(DateUtils.TruncateField field)
    {
        List<Checkmark> checks = getAll();

        int count = 0;
        Timestamp truncatedTimestamps[] = new Timestamp[checks.size()];
        int values[] = new int[checks.size()];

        for (Checkmark rep : checks)
        {
            Timestamp tt = rep.getTimestamp().truncate(field);
            if (count == 0 || !truncatedTimestamps[count - 1].equals(tt))
                truncatedTimestamps[count++] = tt;

            if(habit.isNumerical())
                values[count - 1] += rep.getValue();
            else if(rep.getValue() == Checkmark.CHECKED_EXPLICITLY)
                values[count - 1] += 1000;
        }

        ArrayList<Checkmark> groupedCheckmarks = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            Checkmark rep = new Checkmark(truncatedTimestamps[i], values[i]);
            groupedCheckmarks.add(rep);
        }

        return groupedCheckmarks;
    }
}
