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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Cache;

import org.isoron.uhabits.utils.DateUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class ScoreList
{
    protected final Habit habit;

    protected ModelObservable observable;

    /**
     * Creates a new ScoreList for the given habit.
     * <p>
     * The list is populated automatically according to the repetitions that the
     * habit has.
     *
     * @param habit the habit to which the scores belong.
     */
    public ScoreList(Habit habit)
    {
        this.habit = habit;
        observable = new ModelObservable();
    }

    /**
     * Returns the values of all the scores, from day of the first repetition
     * until today, grouped in chunks of specified size.
     * <p>
     * If the group size is one, then the value of each score is returned
     * individually. If the group is, for example, seven, then the days are
     * grouped in groups of seven consecutive days.
     * <p>
     * The values are returned in an array of integers, with one entry for each
     * group of days in the interval. This value corresponds to the average of
     * the scores for the days inside the group. The first entry corresponds to
     * the ending of the interval (that is, the most recent group of days). The
     * last entry corresponds to the beginning of the interval. As usual, the
     * time of the day for the timestamps should be midnight (UTC). The
     * endpoints of the interval are included.
     * <p>
     * The values are returned in an integer array. There is one entry for each
     * day inside the interval. The first entry corresponds to today, while the
     * last entry corresponds to the day of the oldest repetition.
     *
     * @param divisor the size of the groups
     * @return array of values, with one entry for each group of days
     */
    @NonNull
    public int[] getAllValues(long divisor)
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return new int[0];

        long fromTimestamp = oldestRep.getTimestamp();
        long toTimestamp = DateUtils.getStartOfToday();
        return getValues(fromTimestamp, toTimestamp, divisor);
    }

    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns the value of the score for today.
     *
     * @return value of today's score
     */
    public int getTodayValue()
    {
        return getValue(DateUtils.getStartOfToday());
    }

    /**
     * Returns the value of the score for a given day.
     *
     * @param timestamp the timestamp of a day
     * @return score for that day
     */
    public abstract int getValue(long timestamp);

    /**
     * Marks all scores that have timestamp equal to or newer than the given
     * timestamp as invalid. Any following getValue calls will trigger the
     * scores to be recomputed.
     *
     * @param timestamp the oldest timestamp that should be invalidated
     */
    public abstract void invalidateNewerThan(long timestamp);

    public void writeCSV(Writer out) throws IOException
    {
        computeAll();

        SimpleDateFormat dateFormat = DateUtils.getCSVDateFormat();

        String query =
            "select timestamp, score from score where habit = ? order by timestamp";
        String params[] = {habit.getId().toString()};

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if (!cursor.moveToFirst()) return;

        do
        {
            String timestamp = dateFormat.format(new Date(cursor.getLong(0)));
            String score = String.format("%.4f",
                ((float) cursor.getInt(1)) / Score.MAX_VALUE);
            out.write(String.format("%s,%s\n", timestamp, score));

        } while (cursor.moveToNext());

        cursor.close();
        out.close();
    }

    protected abstract void add(List<Score> scores);

    /**
     * Computes and saves the scores that are missing inside a given time
     * interval.
     * <p>
     * Scores that have already been computed are skipped, therefore there is no
     * harm in calling this function more times, or with larger intervals, than
     * strictly needed. The endpoints of the interval are included.
     * <p>
     * This function assumes that there are no gaps on the scores. That is, if
     * the newest score has timestamp t, then every score with timestamp lower
     * than t has already been computed.
     *
     * @param from timestamp of the beginning of the interval
     * @param to   timestamp of the end of the time interval
     */
    protected void compute(long from, long to)
    {
        final long day = DateUtils.millisecondsInOneDay;
        final double freq = ((double) habit.getFreqNum()) / habit.getFreqDen();

        int newestValue = 0;
        long newestTimestamp = 0;

        Score newest = getNewestComputed();
        if(newest != null)
        {
            newestValue = newest.getValue();
            newestTimestamp = newest.getTimestamp();
        }

        if (newestTimestamp > 0) from = newestTimestamp + day;

        final int checkmarkValues[] = habit.getCheckmarks().getValues(from, to);
        final long beginning = from;

        int lastScore = newestValue;
        List<Score> scores = new LinkedList<>();

        for (int i = 0; i < checkmarkValues.length; i++)
        {
            int value = checkmarkValues[checkmarkValues.length - i - 1];
            lastScore = Score.compute(freq, lastScore, value);
            scores.add(new Score(habit, beginning + day * i, lastScore));
        }

        add(scores);
    }

    /**
     * Computes and saves the scores that are missing since the first repetition
     * of the habit.
     */
    protected void computeAll()
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return;

        long toTimestamp = DateUtils.getStartOfToday();
        compute(oldestRep.getTimestamp(), toTimestamp);
    }

    /**
     * Returns the score for a certain day.
     *
     * @param timestamp the timestamp for the day
     * @return the score for the day
     */
    protected abstract Score get(long timestamp);

    /**
     * Returns the most recent score that was already computed.
     * <p>
     * If no score has been computed yet, returns null.
     *
     * @return the newest score computed, or null if none exist
     */
    @Nullable
    protected abstract Score getNewestComputed();

    /**
     * Same as getAllValues(long), but using a specified interval.
     *
     * @param from    beginning of the interval (included)
     * @param to      end of the interval (included)
     * @param divisor size of the groups
     * @return array of values, with one entry for each group of days
     */
    protected abstract int[] getValues(long from, long to, long divisor);
}
