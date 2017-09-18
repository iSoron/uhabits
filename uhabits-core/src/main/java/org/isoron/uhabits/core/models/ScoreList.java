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

import org.isoron.uhabits.core.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;

public abstract class ScoreList implements Iterable<Score>
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
     * Adds the given scores to the list.
     * <p>
     * This method should not be called by the application, since the scores are
     * computed automatically from the list of repetitions.
     *
     * @param scores the scores to add.
     */
    public abstract void add(List<Score> scores);

    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns the value of the score for today.
     *
     * @return value of today's score
     */
    public double getTodayValue()
    {
        return getValue(DateUtils.getToday());
    }

    /**
     * Returns the value of the score for a given day.
     * <p>
     * If the timestamp given happens before the first repetition of the habit
     * then returns zero.
     *
     * @param timestamp the timestamp of a day
     * @return score value for that day
     */
    public final synchronized double getValue(Timestamp timestamp)
    {
        compute(timestamp, timestamp);
        Score s = getComputedByTimestamp(timestamp);
        if (s == null) throw new IllegalStateException();
        return s.getValue();
    }

    /**
     * Returns the list of scores that fall within the given interval.
     * <p>
     * There is exactly one score per day in the interval. The endpoints of
     * the interval are included. The list is ordered by timestamp (decreasing).
     * That is, the first score corresponds to the newest timestamp, and the
     * last score corresponds to the oldest timestamp.
     *
     * @param fromTimestamp timestamp of the beginning of the interval.
     * @param toTimestamp   timestamp of the end of the interval.
     * @return the list of scores within the interval.
     */
    @NonNull
    public abstract List<Score> getByInterval(@NonNull Timestamp fromTimestamp,
                                              @NonNull Timestamp toTimestamp);

    /**
     * Returns the values of the scores that fall inside a certain interval
     * of time.
     * <p>
     * The values are returned in an array containing one integer value for each
     * day of the interval. The first entry corresponds to the most recent day
     * in the interval. Each subsequent entry corresponds to one day older than
     * the previous entry. The boundaries of the time interval are included.
     *
     * @param from timestamp for the oldest score
     * @param to   timestamp for the newest score
     * @return values for the scores inside the given interval
     */
    public final double[] getValues(Timestamp from, Timestamp to)
    {
        List<Score> scores = getByInterval(from, to);
        double[] values = new double[scores.size()];

        for (int i = 0; i < values.length; i++)
            values[i] = scores.get(i).getValue();

        return values;
    }

    public List<Score> groupBy(DateUtils.TruncateField field)
    {
        computeAll();
        HashMap<Timestamp, ArrayList<Double>> groups = getGroupedValues(field);
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
    public abstract void invalidateNewerThan(Timestamp timestamp);

    @Override
    public Iterator<Score> iterator()
    {
        return toList().iterator();
    }

    /**
     * Returns a Java list of scores, containing one score for each day, from
     * the first repetition of the habit until today.
     * <p>
     * The scores are sorted by decreasing timestamp. The first score
     * corresponds to today.
     *
     * @return list of scores
     */
    public abstract List<Score> toList();

    public void writeCSV(Writer out) throws IOException
    {
        computeAll();
        SimpleDateFormat dateFormat = DateFormats.getCSVDateFormat();

        for (Score s : this)
        {
            String timestamp = dateFormat.format(s.getTimestamp().getUnixTime());
            String score = String.format((Locale)null, "%.4f", s.getValue());
            out.write(String.format("%s,%s\n", timestamp, score));
        }
    }

    /**
     * Computes and stores one score for each day inside the given interval.
     * <p>
     * Scores that have already been computed are skipped, therefore there is no
     * harm in calling this function more times, or with larger intervals, than
     * strictly needed. The endpoints of the interval are included.
     * <p>
     * This method assumes the list of computed scores has no holes. That is, if
     * there is a score computed at time t1 and another at time t2, then every
     * score between t1 and t2 is also computed.
     *
     * @param from timestamp of the beginning of the interval
     * @param to   timestamp of the end of the time interval
     */
    protected synchronized void compute(@NonNull Timestamp from,
                                        @NonNull Timestamp to)
    {
        Score newest = getNewestComputed();
        Score oldest = getOldestComputed();

        if (newest == null)
        {
            Repetition oldestRep = habit.getRepetitions().getOldest();
            if (oldestRep != null) from =
                Timestamp.oldest(from, oldestRep.getTimestamp());
            forceRecompute(from, to, 0);
        }
        else
        {
            if (oldest == null) throw new IllegalStateException();
            forceRecompute(from, oldest.getTimestamp().minus(1), 0);
            forceRecompute(newest.getTimestamp().plus(1), to,
                newest.getValue());
        }
    }

    /**
     * Computes and saves the scores that are missing since the first repetition
     * of the habit.
     */
    protected void computeAll()
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return;

        Timestamp today = DateUtils.getToday();
        compute(oldestRep.getTimestamp(), today);
    }

    /**
     * Returns the score that has the given timestamp, if it has already been
     * computed. If that score has not been computed yet, returns null.
     *
     * @param timestamp the timestamp of the score
     * @return the score with given timestamp, or null not yet computed.
     */
    @Nullable
    protected abstract Score getComputedByTimestamp(Timestamp timestamp);

    /**
     * Returns the most recent score that has already been computed. If no score
     * has been computed yet, returns null.
     */
    @Nullable
    protected abstract Score getNewestComputed();

    /**
     * Returns oldest score already computed. If no score has been computed yet,
     * returns null.
     */
    @Nullable
    protected abstract Score getOldestComputed();

    /**
     * Computes and stores one score for each day inside the given interval.
     * <p>
     * This function does not check if the scores have already been computed. If
     * they have, then it stores duplicate scores, which is a bad thing.
     *
     * @param from          timestamp of the beginning of the interval
     * @param to            timestamp of the end of the interval
     * @param previousValue value of the score on the day immediately before the
     *                      interval begins
     */
    private void forceRecompute(@NonNull Timestamp from,
                                @NonNull Timestamp to,
                                double previousValue)
    {
        if (from.isNewerThan(to)) return;

        final double freq = habit.getFrequency().toDouble();
        final int checkmarkValues[] = habit.getCheckmarks().getValues(from, to);

        List<Score> scores = new LinkedList<>();

        for (int i = 0; i < checkmarkValues.length; i++)
        {
            double value = checkmarkValues[checkmarkValues.length - i - 1];

            if (habit.isNumerical())
            {
                value /= 1000;
                value /= habit.getTargetValue();
                value = Math.min(1, value);
            }

            if (!habit.isNumerical() && value > 0) value = 1;

            previousValue = Score.compute(freq, previousValue, value);
            scores.add(new Score(from.plus(i), previousValue));
        }

        add(scores);
    }

    @NonNull
    private HashMap<Timestamp, ArrayList<Double>> getGroupedValues(DateUtils.TruncateField field)
    {
        HashMap<Timestamp, ArrayList<Double>> groups = new HashMap<>();

        for (Score s : this)
        {
            Timestamp groupTimestamp = new Timestamp(
                DateUtils.truncate(field, s.getTimestamp().getUnixTime()));

            if (!groups.containsKey(groupTimestamp))
                groups.put(groupTimestamp, new ArrayList<>());

            groups.get(groupTimestamp).add(s.getValue());
        }

        return groups;
    }

    @NonNull
    private List<Score> groupsToAvgScores(HashMap<Timestamp, ArrayList<Double>> groups)
    {
        List<Score> scores = new LinkedList<>();

        for (Timestamp timestamp : groups.keySet())
        {
            double meanValue = 0.0;
            ArrayList<Double> groupValues = groups.get(timestamp);

            for (Double v : groupValues) meanValue += v;
            meanValue /= groupValues.size();

            scores.add(new Score(timestamp, meanValue));
        }

        return scores;
    }
}
