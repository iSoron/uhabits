/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.activities.habits.overview

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Timestamp

/**
 * Calculates aggregate scores across multiple habits over a time period.
 */
class AggregateScoreCalculator {

    /**
     * Computes the average score across all given habits for each day in the specified range.
     *
     * @param habits List of habits to aggregate
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return List of aggregate scores, one per day, ordered from oldest to newest
     */
    fun computeAggregateScores(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): List<Score> {
        if (habits.isEmpty()) {
            return emptyList()
        }

        if (fromDate.isNewerThan(toDate)) {
            return emptyList()
        }

        val aggregateScores = mutableListOf<Score>()
        var current = fromDate

        while (!current.isNewerThan(toDate)) {
            var sumScore = 0.0
            var count = 0

            for (habit in habits) {
                val score = habit.scores[current]
                sumScore += score.value
                count++
            }

            val avgScore = if (count > 0) sumScore / count else 0.0
            aggregateScores.add(Score(current, avgScore))
            current = current.plus(1)
        }

        return aggregateScores
    }

    /**
     * Finds the earliest date among all habits that have at least one entry.
     *
     * @param habits List of habits to search
     * @param defaultDate Default date to return if no habits have entries
     * @return The earliest timestamp found, or defaultDate if none found
     */
    fun findEarliestHabitDate(habits: List<Habit>, defaultDate: Timestamp): Timestamp {
        if (habits.isEmpty()) {
            return defaultDate
        }

        var earliest = defaultDate

        for (habit in habits) {
            val entries = habit.originalEntries.getKnown()
            if (entries.isNotEmpty()) {
                val firstEntry = entries.minByOrNull { it.timestamp.unixTime }
                if (firstEntry != null && firstEntry.timestamp.isOlderThan(earliest)) {
                    earliest = firstEntry.timestamp
                }
            }
        }

        return earliest
    }

    /**
     * Calculates all improvement streaks from the score history.
     * A streak is a consecutive period where each day's score is strictly greater than the previous day.
     * Equal scores do not count as improvement.
     *
     * @param scores List of scores ordered from oldest to newest
     * @return List of the last 10 streaks (or fewer if less than 10 exist), ordered by recency
     */
    fun calculateAggregateStreaks(scores: List<Score>): List<org.isoron.uhabits.core.models.Streak> {
        if (scores.size < 2) {
            return emptyList()
        }

        val allStreaks = mutableListOf<org.isoron.uhabits.core.models.Streak>()
        var streakStart: Timestamp? = null
        var streakEnd: Timestamp? = null

        for (i in 1 until scores.size) {
            val currentScore = scores[i].value
            val previousScore = scores[i - 1].value

            // Check for strict improvement (not equal)
            if (currentScore > previousScore) {
                if (streakStart == null) {
                    // Start a new streak - starts on the first improved day, not the baseline
                    streakStart = scores[i].timestamp
                    streakEnd = scores[i].timestamp
                } else {
                    // Continue the current streak
                    streakEnd = scores[i].timestamp
                }
            } else {
                // Streak broken - save if we had one
                if (streakStart != null && streakEnd != null) {
                    allStreaks.add(org.isoron.uhabits.core.models.Streak(streakStart, streakEnd))
                }
                streakStart = null
                streakEnd = null
            }
        }

        // Don't forget to save the last streak if it was ongoing
        if (streakStart != null && streakEnd != null) {
            allStreaks.add(org.isoron.uhabits.core.models.Streak(streakStart, streakEnd))
        }

        // Return last 10 streaks, reversed so most recent appears first
        val last10 = if (allStreaks.size <= 10) {
            allStreaks
        } else {
            allStreaks.takeLast(10)
        }
        
        return last10.reversed()
    }

    /**
     * Computes aggregate weekday frequency data for use in frequency charts.
     * For each month in the date range, calculates the average score for each weekday.
     * 
     * @param habits List of habits to aggregate
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return HashMap where key is first day of month, value is array of 7 integers
     *         representing average scores (0-1000) for each weekday (Sat=0, Sun=1, ... Fri=6)
     */
    fun computeAggregateWeekdayFrequency(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): HashMap<Timestamp, Array<Int>> {
        if (habits.isEmpty()) {
            return hashMapOf()
        }

        // First compute all daily aggregate scores
        val scores = computeAggregateScores(habits, fromDate, toDate)
        if (scores.isEmpty()) {
            return hashMapOf()
        }

        // Group scores by month and weekday
        val map = hashMapOf<Timestamp, HashMap<Int, MutableList<Double>>>()
        
        for (score in scores) {
            val timestamp = score.timestamp
            val weekday = timestamp.weekday
            
            // Truncate to first day of month
            val truncatedTimestamp = Timestamp(
                timestamp.toCalendar().apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis
            )
            
            // Get or create month map
            val monthMap = map.getOrPut(truncatedTimestamp) {
                hashMapOf()
            }
            
            // Get or create weekday list
            val weekdayScores = monthMap.getOrPut(weekday) {
                mutableListOf()
            }
            
            weekdayScores.add(score.value)
        }
        
        // Convert to final format: average scores as integers (0-1000)
        val result = hashMapOf<Timestamp, Array<Int>>()
        
        for ((monthTimestamp, monthMap) in map) {
            val weekdayAverages = Array(7) { 0 }
            
            for (weekday in 0..6) {
                val scores = monthMap[weekday]
                if (scores != null && scores.isNotEmpty()) {
                    val average = scores.average()
                    // Convert to integer (0-1000 range) to match FrequencyChart expectations
                    weekdayAverages[weekday] = (average * 1000).toInt()
                }
            }
            
            result[monthTimestamp] = weekdayAverages
        }
        
        return result
    }
}

