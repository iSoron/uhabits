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

package org.isoron.uhabits.models;

import android.support.annotation.*;

import org.isoron.uhabits.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * The collection of {@link Checkmark}s belonging to a habit.
 */
public abstract class CheckmarkList
{
    protected Habit habit;

    public ModelObservable observable = new ModelObservable();

    public CheckmarkList(Habit habit)
    {
        this.habit = habit;
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
    public final int[] getAllValues()
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return new int[0];

        Long fromTimestamp = oldestRep.getTimestamp();
        Long toTimestamp = DateUtils.getStartOfToday();

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
    public abstract List<Checkmark> getByInterval(long fromTimestamp,
                                                  long toTimestamp);

    /**
     * Returns the checkmark for today.
     *
     * @return checkmark for today
     */
    @Nullable
    public final Checkmark getToday()
    {
        computeAll();
        return getNewestComputed();
    }

    /**
     * Returns the value of today's checkmark.
     *
     * @return value of today's checkmark
     */
    public final int getTodayValue()
    {
        Checkmark today = getToday();
        if (today != null) return today.getValue();
        else return Checkmark.UNCHECKED;
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
    public final int[] getValues(long from, long to)
    {
        if(from > to) return new int[0];

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
    public abstract void invalidateNewerThan(long timestamp);

    /**
     * Writes the entire list of checkmarks to the given writer, in CSV format.
     *
     * @param out the writer where the CSV will be output
     * @throws IOException in case write operations fail
     */
    public final void writeCSV(Writer out) throws IOException
    {
        computeAll();

        int values[] = getAllValues();
        long timestamp = DateUtils.getStartOfToday();
        SimpleDateFormat dateFormat = DateFormats.getCSVDateFormat();

        for (int value : values)
        {
            String date = dateFormat.format(new Date(timestamp));
            out.write(String.format("%s,%d\n", date, value));
            timestamp -= DateUtils.millisecondsInOneDay;
        }
    }

    /**
     * Computes and stores one checkmark for each day that falls inside the
     * specified interval of time. Days that already have a corresponding
     * checkmark are skipped.
     *
     * This method assumes the list of computed checkmarks has no holes. That
     * is, if there is a checkmark computed at time t1 and another at time t2,
     * then every checkmark between t1 and t2 is also computed.
     *
     * @param from timestamp for the beginning of the interval
     * @param to   timestamp for the end of the interval
     */
    protected final synchronized void compute(long from, long to)
    {
        final long day = DateUtils.millisecondsInOneDay;

        Checkmark newest = getNewestComputed();
        Checkmark oldest = getOldestComputed();

        if (newest == null)
        {
            forceRecompute(from, to);
        }
        else
        {
            forceRecompute(from, oldest.getTimestamp() - day);
            forceRecompute(newest.getTimestamp() + day, to);
        }
    }

    /**
     * Returns oldest checkmark that has already been computed.
     *
     * @return oldest checkmark already computed
     */
    protected abstract Checkmark getOldestComputed();

    /**
     * Computes and stores one checkmark for each day that falls inside the
     * specified interval of time.
     *
     * This method does not check if the checkmarks have already been
     * computed or not. If they have, then duplicate checkmarks will
     * be stored, which is a bad thing.
     *
     * @param from timestamp for the beginning of the interval
     * @param to   timestamp for the end of the interval
     */
    private synchronized void forceRecompute(long from, long to)
    {
        if (from > to) return;

        final long day = DateUtils.millisecondsInOneDay;
        Frequency freq = habit.getFrequency();

        long fromExtended = from - (long) (freq.getDenominator()) * day;
        List<Repetition> reps =
            habit.getRepetitions().getByInterval(fromExtended, to);

        final int nDays = (int) ((to - from) / day) + 1;
        int nDaysExtended = (int) ((to - fromExtended) / day) + 1;
        final int checks[] = new int[nDaysExtended];

        for (Repetition rep : reps)
        {
            int offset = (int) ((rep.getTimestamp() - fromExtended) / day);
            checks[nDaysExtended - offset - 1] = Checkmark.CHECKED_EXPLICITLY;
        }

        for (int i = 0; i < nDays; i++)
        {
            int counter = 0;

            for (int j = 0; j < freq.getDenominator(); j++)
                if (checks[i + j] == 2) counter++;

            if (counter >= freq.getNumerator())
                if (checks[i] != Checkmark.CHECKED_EXPLICITLY)
                    checks[i] = Checkmark.CHECKED_IMPLICITLY;
        }

        List<Checkmark> checkmarks = new LinkedList<>();

        for (int i = 0; i < nDays; i++)
        {
            int value = checks[i];
            long timestamp = to - i * day;
            checkmarks.add(new Checkmark(timestamp, value));
        }

        add(checkmarks);
    }

    /**
     * Computes and stores one checkmark for each day, since the first
     * repetition of the habit until today. Days that already have a
     * corresponding checkmark are skipped.
     */
    protected final void computeAll()
    {
        Repetition oldest = habit.getRepetitions().getOldest();
        if (oldest == null) return;

        Long today = DateUtils.getStartOfToday();
        compute(oldest.getTimestamp(), today);
    }

    /**
     * Returns newest checkmark that has already been computed.
     *
     * @return newest checkmark already computed
     */
    protected abstract Checkmark getNewestComputed();
}
