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
     * Calculates the current improvement streak.
     * Counts consecutive days where the score increased compared to the previous day.
     *
     * @param scores List of scores ordered from oldest to newest
     * @return Number of consecutive days of improvement (0 if empty or no improvement)
     */
    fun calculateImprovementStreak(scores: List<Score>): Int {
        if (scores.size < 2) {
            return 0
        }

        var streak = 0
        var i = scores.size - 1

        // Start from most recent day and work backwards
        while (i > 0) {
            val currentScore = scores[i].value
            val previousScore = scores[i - 1].value

            if (currentScore > previousScore) {
                streak++
                i--
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Calculates the longest improvement streak in the given scores.
     *
     * @param scores List of scores ordered from oldest to newest
     * @return Longest streak of consecutive improvements
     */
    fun calculateLongestStreak(scores: List<Score>): Int {
        if (scores.size < 2) {
            return 0
        }

        var longestStreak = 0
        var currentStreak = 0

        for (i in 1 until scores.size) {
            if (scores[i].value > scores[i - 1].value) {
                currentStreak++
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
        }

        return longestStreak
    }

    /**
     * Generates a motivational insight based on streak data.
     *
     * @param currentStreak Current consecutive days of improvement
     * @param longestStreak Longest streak achieved
     * @param recentTrend Average change in score over recent days (positive = improving)
     * @return Motivational message string
     */
    fun generateStreakInsight(
        currentStreak: Int,
        longestStreak: Int,
        recentTrend: Double
    ): String {
        return when {
            // Current streak is a new personal best
            currentStreak > 0 && currentStreak == longestStreak && currentStreak >= 3 ->
                "New personal best!"

            // Strong ongoing streak
            currentStreak >= 7 -> "Building momentum!"

            // Good progress
            currentStreak >= 3 -> "Keep going!"

            // Positive trend but no current streak
            currentStreak == 0 && recentTrend > 0 -> "Steady and consistent!"

            // No streak but previous best exists
            currentStreak == 0 && longestStreak >= 3 -> "Tomorrow's a fresh start!"

            // Starting out or flat performance
            else -> "Take it one day at a time."
        }
    }

    /**
     * Calculates the next milestone for the improvement streak.
     *
     * @param currentStreak Current streak value
     * @return Next milestone value
     */
    fun getNextMilestone(currentStreak: Int): Int {
        return when {
            currentStreak < 3 -> 3
            currentStreak < 7 -> 7
            currentStreak < 14 -> 14
            currentStreak < 30 -> 30
            currentStreak < 60 -> 60
            currentStreak < 90 -> 90
            else -> ((currentStreak / 30) + 1) * 30  // Next 30-day milestone
        }
    }
}

