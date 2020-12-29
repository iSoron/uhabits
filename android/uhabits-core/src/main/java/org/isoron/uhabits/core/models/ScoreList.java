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

import java.util.*;

import static org.isoron.uhabits.core.models.Entry.*;

public class ScoreList
{
    private final HashMap<Timestamp, Score> list = new HashMap<>();

    /**
     * Returns the score for a given day. If the timestamp given happens before the first
     * repetition of the habit or after the last computed score, returns a score with value zero.
     */
    public final synchronized Score get(Timestamp timestamp)
    {
        if (list.containsKey(timestamp)) return list.get(timestamp);
        return new Score(timestamp, 0);
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
    public List<Score> getByInterval(@NonNull Timestamp fromTimestamp,
                                     @NonNull Timestamp toTimestamp)
    {
        List<Score> result = new LinkedList<>();
        if (fromTimestamp.isNewerThan(toTimestamp)) return result;
        Timestamp current = toTimestamp;
        while(!current.isOlderThan(fromTimestamp))
        {
            result.add(get(current));
            current = current.minus(1);
        }
        return result;
    }

    public void recompute(
            Frequency frequency,
            boolean isNumerical,
            double targetValue,
            EntryList computedEntries,
            Timestamp from,
            Timestamp to)
    {
        list.clear();
        if (computedEntries.getKnown().isEmpty()) return;
        if (from.isNewerThan(to)) return;

        double rollingSum = 0.0;
        int numerator = frequency.getNumerator();
        int denominator = frequency.getDenominator();
        final double freq = frequency.toDouble();
        final Integer[] values = computedEntries
                .getByInterval(from, to)
                .stream()
                .map(Entry::getValue)
                .toArray(Integer[]::new);

        // For non-daily boolean habits, we double the numerator and the denominator to smooth
        // out irregular repetition schedules (for example, weekly habits performed on different
        // days of the week)
        if (!isNumerical && freq < 1.0)
        {
            numerator *= 2;
            denominator *= 2;
        }

        double previousValue = 0;
        for (int i = 0; i < values.length; i++)
        {
            int offset = values.length - i - 1;
            if (isNumerical)
            {
                rollingSum += values[offset];
                if (offset + denominator < values.length)
                {
                    rollingSum -= values[offset + denominator];
                }
                double percentageCompleted = Math.min(1, rollingSum / 1000 / targetValue);
                previousValue = Score.compute(freq, previousValue, percentageCompleted);
            }
            else
            {
                if (values[offset] == YES_MANUAL)
                    rollingSum += 1.0;
                if (offset + denominator < values.length)
                    if (values[offset + denominator] == YES_MANUAL)
                        rollingSum -= 1.0;
                if (values[offset] != SKIP)
                {
                    double percentageCompleted = Math.min(1, rollingSum / numerator);
                    previousValue = Score.compute(freq, previousValue, percentageCompleted);
                }
            }
            Timestamp timestamp = from.plus(i);
            list.put(timestamp, new Score(timestamp, previousValue));
        }
    }
}
