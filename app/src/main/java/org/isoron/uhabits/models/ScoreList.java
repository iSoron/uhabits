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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    public abstract List<Score> getAll();

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

    public List<Score> groupBy(DateUtils.TruncateField field)
    {
        HashMap<Long, ArrayList<Long>> groups = getGroupedValues(field);
        List<Score> scores = groupsToAvgScores(groups);
        Collections.sort(scores, (s1, s2) -> s2.compareNewer(s1));
        return scores;
    }

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

        for (Score s : getAll())
        {
            String timestamp = dateFormat.format(s.getTimestamp());
            String score =
                String.format("%.4f", ((float) s.getValue()) / Score.MAX_VALUE);
            out.write(String.format("%s,%s\n", timestamp, score));
        }
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
        if (newest != null)
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
    @Nullable
    protected abstract Score get(long timestamp);

    @NonNull
    private HashMap<Long, ArrayList<Long>> getGroupedValues(DateUtils.TruncateField field)
    {
        HashMap<Long, ArrayList<Long>> groups = new HashMap<>();

        for (Score s : getAll())
        {
            long groupTimestamp = DateUtils.truncate(field, s.getTimestamp());

            if (!groups.containsKey(groupTimestamp))
                groups.put(groupTimestamp, new ArrayList<>());

            groups.get(groupTimestamp).add((long) s.getValue());
        }

        return groups;
    }

    /**
     * Returns the most recent score that has already been computed.
     * <p>
     * If no score has been computed yet, returns null.
     *
     * @return the newest score computed, or null if none exist
     */
    @Nullable
    protected abstract Score getNewestComputed();


    @NonNull
    private List<Score> groupsToAvgScores(HashMap<Long, ArrayList<Long>> groups)
    {
        List<Score> scores = new LinkedList<>();

        for (Long timestamp : groups.keySet())
        {
            long meanValue = 0L;
            ArrayList<Long> groupValues = groups.get(timestamp);

            for (Long v : groupValues) meanValue += v;
            meanValue /= groupValues.size();

            scores.add(new Score(habit, timestamp, (int) meanValue));
        }

        return scores;
    }
}
