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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.isoron.uhabits.utils.DateUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
     * Returns the values for all the checkmarks, since the oldest repetition of
     * the habit until today. If there are no repetitions at all, returns an
     * empty array.
     * <p>
     * The values are returned in an array containing one integer value for each
     * day since the first repetition of the habit until today. The first entry
     * corresponds to today, the second entry corresponds to yesterday, and so
     * on.
     *
     * @return values for the checkmarks in the interval
     */
    @NonNull
    public int[] getAllValues()
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return new int[0];

        Long fromTimestamp = oldestRep.getTimestamp();
        Long toTimestamp = DateUtils.getStartOfToday();

        return getValues(fromTimestamp, toTimestamp);
    }

    /**
     * Returns the checkmark for today.
     *
     * @return checkmark for today
     */
    @Nullable
    public Checkmark getToday()
    {
        long today = DateUtils.getStartOfToday();
        compute(today, today);
        return getNewest();
    }

    /**
     * Returns the value of today's checkmark.
     *
     * @return value of today's checkmark
     */
    public int getTodayValue()
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
    public abstract int[] getValues(long from, long to);

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
     * There is one line for each checkmark. Each line contains two fields:
     * timestamp and value.
     *
     * @param out the writer where the CSV will be output
     * @throws IOException in case write operations fail
     */
    public void writeCSV(Writer out) throws IOException
    {
        computeAll();

        int values[] = getAllValues();
        long timestamp = DateUtils.getStartOfToday();
        SimpleDateFormat dateFormat = DateUtils.getCSVDateFormat();

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
     * @param from timestamp for the beginning of the interval
     * @param to   timestamp for the end of the interval
     */
    protected void compute(long from, final long to)
    {
        final long day = DateUtils.millisecondsInOneDay;

        Checkmark newestCheckmark = getNewest();
        if (newestCheckmark != null)
            from = newestCheckmark.getTimestamp() + day;

        if (from > to) return;

        long fromExtended = from - (long) (habit.getFreqDen()) * day;
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

            for (int j = 0; j < habit.getFreqDen(); j++)
                if (checks[i + j] == 2) counter++;

            if (counter >= habit.getFreqNum())
                if (checks[i] != Checkmark.CHECKED_EXPLICITLY)
                    checks[i] = Checkmark.CHECKED_IMPLICITLY;
        }


        long timestamps[] = new long[nDays];
        for (int i = 0; i < nDays; i++)
            timestamps[i] = to - i * day;

        insert(timestamps, checks);
    }

    /**
     * Computes and stores one checkmark for each day, since the first
     * repetition until today. Days that already have a corresponding checkmark
     * are skipped.
     */
    protected void computeAll()
    {
        Repetition oldest = habit.getRepetitions().getOldest();
        if (oldest == null) return;

        Long today = DateUtils.getStartOfToday();

        compute(oldest.getTimestamp(), today);
    }

    /**
     * Returns newest checkmark that has already been computed. Ignores any
     * checkmark that has timestamp in the future. This does not update the
     * cache.
     *
     * @return newest checkmark already computed
     */
    protected abstract Checkmark getNewest();

    protected abstract void insert(long timestamps[], int values[]);
}
