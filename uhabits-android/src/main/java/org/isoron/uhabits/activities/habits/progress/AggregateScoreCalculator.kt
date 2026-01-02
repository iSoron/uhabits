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

package org.isoron.uhabits.activities.habits.progress

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Streak
import org.isoron.uhabits.core.models.Timestamp
import kotlin.math.max

/**
 * Calculates aggregate scores across multiple habits over a time period.
 */
class AggregateScoreCalculator {

    data class CompletionSummary(
        val timestamp: Timestamp,
        val completedCount: Int,
        val dueCount: Int
    ) {
        val completionRatio: Double
            get() = if (dueCount > 0) completedCount.toDouble() / dueCount else 0.0

        val missedCount: Int
            get() = max(0, dueCount - completedCount)
    }

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
     * Computes day-to-day score changes across all habits.
     * The first day in the range has a change of 0.0 by definition.
     */
    fun computeAggregateProgressChanges(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): List<Score> {
        val scores = computeAggregateScores(habits, fromDate, toDate)
        if (scores.isEmpty()) {
            return emptyList()
        }

        val changes = mutableListOf<Score>()
        for (i in scores.indices) {
            val change = if (i == 0) {
                0.0
            } else {
                scores[i].value - scores[i - 1].value
            }
            changes.add(Score(scores[i].timestamp, change))
        }
        return changes
    }

    /**
     * Computes improvement streak length for each day based on aggregate scores.
     * A streak increments when today's score is strictly greater than yesterday's.
     */
    fun computeImprovementStreakLengths(scores: List<Score>): List<Score> {
        if (scores.isEmpty()) {
            return emptyList()
        }

        val streaks = mutableListOf<Score>()
        var currentLength = 0
        streaks.add(Score(scores[0].timestamp, 0.0))

        for (i in 1 until scores.size) {
            currentLength = if (scores[i].value > scores[i - 1].value) {
                currentLength + 1
            } else {
                0
            }
            streaks.add(Score(scores[i].timestamp, currentLength.toDouble()))
        }

        return streaks
    }

    /**
     * Computes descending competition ranks for the provided values.
     * Null values are excluded from ranking.
     */
    fun computeDescendingRanks(values: List<Pair<Timestamp, Double?>>): Map<Timestamp, Int> {
        val filtered = values.filter { it.second != null }
        if (filtered.isEmpty()) {
            return emptyMap()
        }

        val sorted = filtered.sortedWith(
            compareByDescending<Pair<Timestamp, Double?>> { it.second!! }
                .thenByDescending { it.first.unixTime }
        )
        val ranks = mutableMapOf<Timestamp, Int>()
        var currentRank = 0
        var lastValue: Double? = null

        for (index in sorted.indices) {
            val (timestamp, value) = sorted[index]
            if (lastValue == null || value != lastValue) {
                currentRank = index + 1
                lastValue = value
            }
            ranks[timestamp] = currentRank
        }

        return ranks
    }

    /**
     * Computes the number of completed and due habits for each day in the specified range.
     * Due habits exclude auto-skipped entries (YES_AUTO). Manual skips count as completed.
     */
    fun computeAggregateCompletionSummaries(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): List<CompletionSummary> {
        if (habits.isEmpty()) {
            return emptyList()
        }

        if (fromDate.isNewerThan(toDate)) {
            return emptyList()
        }

        val summaries = mutableListOf<CompletionSummary>()
        var current = fromDate

        while (!current.isNewerThan(toDate)) {
            var dueCount = 0
            var completedCount = 0

            for (habit in habits) {
                val entry = habit.computedEntries.get(current)
                if (!isHabitDueForDate(habit, entry)) {
                    continue
                }

                dueCount++
                if (isHabitCompletedForDate(habit, entry)) {
                    completedCount++
                }
            }

            summaries.add(
                CompletionSummary(
                    timestamp = current,
                    completedCount = completedCount,
                    dueCount = dueCount
                )
            )
            current = current.plus(1)
        }

        return summaries
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

        var earliest: Timestamp? = null

        for (habit in habits) {
            val entries = habit.originalEntries.getKnown()
            if (entries.isNotEmpty()) {
                val firstEntry = entries.minByOrNull { it.timestamp.unixTime }
                if (firstEntry != null) {
                    if (earliest == null || firstEntry.timestamp.isOlderThan(earliest)) {
                        earliest = firstEntry.timestamp
                    }
                }
            }
        }

        return earliest ?: defaultDate
    }

    /**
     * Computes completion totals grouped by time period for bar chart display.
     * Each entry represents the total number of completed habits for that period.
     */
    fun computeCompletionEntriesByPeriod(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp,
        truncateField: Int
    ): List<Entry> {
        val summaries = computeAggregateCompletionSummaries(habits, fromDate, toDate)
        if (summaries.isEmpty()) {
            return emptyList()
        }

        val grouped = mutableMapOf<Timestamp, Int>()

        for (summary in summaries) {
            val truncatedTimestamp = if (truncateField == -1) {
                summary.timestamp
            } else {
                Timestamp(
                    summary.timestamp.toCalendar().apply {
                        when (truncateField) {
                            java.util.Calendar.DAY_OF_WEEK -> {
                                set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_MONTH -> {
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.MONTH -> {
                                val currentMonth = get(java.util.Calendar.MONTH)
                                val quarterStartMonth = (currentMonth / 3) * 3
                                set(java.util.Calendar.MONTH, quarterStartMonth)
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_YEAR -> {
                                set(java.util.Calendar.DAY_OF_YEAR, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                        }
                    }.timeInMillis
                )
            }

            grouped[truncatedTimestamp] =
                (grouped[truncatedTimestamp] ?: 0) + summary.completedCount
        }

        return grouped.map { (timestamp, totalCompleted) ->
            Entry(timestamp = timestamp, value = totalCompleted)
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Computes aggregate progress change per weekday for each month.
     * Values are returned in 0-1000 range (average change * 1000), preserving sign.
     */
    fun computeAggregateProgressChangeWeekdayFrequency(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): HashMap<Timestamp, Array<Int>> {
        val changes = computeAggregateProgressChanges(habits, fromDate, toDate)
        if (changes.isEmpty()) {
            return hashMapOf()
        }

        val map = hashMapOf<Timestamp, HashMap<Int, MutableList<Double>>>()

        for (change in changes) {
            val timestamp = change.timestamp
            val weekday = timestamp.weekday
            val truncatedTimestamp = Timestamp(
                timestamp.toCalendar().apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis
            )

            val monthMap = map.getOrPut(truncatedTimestamp) {
                hashMapOf()
            }
            val weekdayChanges = monthMap.getOrPut(weekday) {
                mutableListOf()
            }
            weekdayChanges.add(change.value)
        }

        val result = hashMapOf<Timestamp, Array<Int>>()
        for ((monthTimestamp, monthMap) in map) {
            val weekdayAverages = Array(7) { 0 }
            for (weekday in 0..6) {
                val ratios = monthMap[weekday]
                if (ratios != null && ratios.isNotEmpty()) {
                    val average = ratios.average()
                    weekdayAverages[weekday] = (average * 1000).toInt()
                }
            }
            result[monthTimestamp] = weekdayAverages
        }

        return result
    }

    /**
     * Computes aggregate completion ratio per weekday for each month.
     * Values are returned in 0-1000 range (percentage * 10).
     */
    fun computeCompletionWeekdayFrequency(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp
    ): HashMap<Timestamp, Array<Int>> {
        val summaries = computeAggregateCompletionSummaries(habits, fromDate, toDate)
        if (summaries.isEmpty()) {
            return hashMapOf()
        }

        val map = hashMapOf<Timestamp, HashMap<Int, MutableList<Double>>>()

        for (summary in summaries) {
            if (summary.dueCount <= 0) {
                continue
            }

            val timestamp = summary.timestamp
            val weekday = timestamp.weekday
            val truncatedTimestamp = Timestamp(
                timestamp.toCalendar().apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis
            )

            val monthMap = map.getOrPut(truncatedTimestamp) {
                hashMapOf()
            }
            val weekdayRatios = monthMap.getOrPut(weekday) {
                mutableListOf()
            }
            weekdayRatios.add(summary.completionRatio)
        }

        val result = hashMapOf<Timestamp, Array<Int>>()
        for ((monthTimestamp, monthMap) in map) {
            val weekdayAverages = Array(7) { 0 }
            for (weekday in 0..6) {
                val ratios = monthMap[weekday]
                if (ratios != null && ratios.isNotEmpty()) {
                    val average = ratios.average()
                    weekdayAverages[weekday] = (average * 1000).toInt()
                }
            }
            result[monthTimestamp] = weekdayAverages
        }

        return result
    }

    /**
     * Calculates best streaks of perfect completion (100% due habits completed).
     */
    fun calculateCompletionStreaks(summaries: List<CompletionSummary>): List<Streak> {
        if (summaries.isEmpty()) {
            return emptyList()
        }

        val allStreaks = mutableListOf<Streak>()
        var streakStart: Timestamp? = null
        var streakEnd: Timestamp? = null

        for (summary in summaries) {
            val isPerfectDay = summary.dueCount > 0 && summary.completedCount == summary.dueCount
            if (isPerfectDay) {
                if (streakStart == null) {
                    streakStart = summary.timestamp
                    streakEnd = summary.timestamp
                } else {
                    streakEnd = summary.timestamp
                }
            } else {
                if (streakStart != null && streakEnd != null) {
                    allStreaks.add(Streak(streakStart, streakEnd))
                }
                streakStart = null
                streakEnd = null
            }
        }

        if (streakStart != null && streakEnd != null) {
            allStreaks.add(Streak(streakStart, streakEnd))
        }

        if (allStreaks.isEmpty()) {
            return emptyList()
        }

        val bestStreaks = allStreaks.sortedWith { s1, s2 ->
            s2.compareLonger(s1)
        }.take(10)

        return bestStreaks.sortedWith { s1, s2 ->
            s2.compareNewer(s1)
        }
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

    /**
     * Computes aggregate entries grouped by time period for bar chart display.
     * Each entry represents the average aggregate score for that period.
     * 
     * @param habits List of habits to aggregate
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @param bucketSize Period size (7=week, 31=month, 92=quarter, 365=year)
     * @param truncateField Calendar field to truncate by (DAY_OF_WEEK, DAY_OF_MONTH, MONTH, DAY_OF_YEAR)
     * @return List of Entry where value represents average score * 1000 (0-1000 range)
     */
    fun computeAggregateEntriesByPeriod(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp,
        bucketSize: Int,
        truncateField: Int
    ): List<org.isoron.uhabits.core.models.Entry> {
        // Get all daily aggregate scores
        val scores = computeAggregateScores(habits, fromDate, toDate)
        if (scores.isEmpty()) {
            return emptyList()
        }

        // Group scores by period
        val grouped = mutableMapOf<Timestamp, MutableList<Double>>()
        
        for (score in scores) {
            // For daily view (truncateField = -1), use the score timestamp directly without truncation
            val truncatedTimestamp = if (truncateField == -1) {
                score.timestamp
            } else {
                Timestamp(
                    score.timestamp.toCalendar().apply {
                        when (truncateField) {
                            java.util.Calendar.DAY_OF_WEEK -> {
                                // Truncate to start of week
                                set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_MONTH -> {
                                // Truncate to start of month
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.MONTH -> {
                                // Truncate to start of quarter (Q1=Jan, Q2=Apr, Q3=Jul, Q4=Oct)
                                val currentMonth = get(java.util.Calendar.MONTH)
                                val quarterStartMonth = (currentMonth / 3) * 3 // 0, 3, 6, or 9
                                set(java.util.Calendar.MONTH, quarterStartMonth)
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_YEAR -> {
                                // Truncate to start of year
                                set(java.util.Calendar.DAY_OF_YEAR, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                        }
                    }.timeInMillis
                )
            }
            
            grouped.getOrPut(truncatedTimestamp) { mutableListOf() }.add(score.value)
        }
        
        // Convert to Entry list with average values (multiply by 1000 to match Entry format)
        // Sort by descending timestamp so most recent periods appear on the right in BarChart
        return grouped.map { (timestamp, values) ->
            val average = values.average()
            org.isoron.uhabits.core.models.Entry(
                timestamp = timestamp,
                value = (average * 1000).toInt() // Convert 0-1 score to 0-1000 range
            )
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Computes aggregate scores grouped by time period for delta chart display.
     * Each score represents the average aggregate score for that period (0-1 range).
     */
    fun computeAggregateScoresByPeriod(
        habits: List<Habit>,
        fromDate: Timestamp,
        toDate: Timestamp,
        bucketSize: Int,
        truncateField: Int
    ): List<Score> {
        val scores = computeAggregateScores(habits, fromDate, toDate)
        if (scores.isEmpty()) {
            return emptyList()
        }

        val grouped = mutableMapOf<Timestamp, MutableList<Double>>()

        for (score in scores) {
            val truncatedTimestamp = if (truncateField == -1) {
                score.timestamp
            } else {
                Timestamp(
                    score.timestamp.toCalendar().apply {
                        when (truncateField) {
                            java.util.Calendar.DAY_OF_WEEK -> {
                                set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_MONTH -> {
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.MONTH -> {
                                val currentMonth = get(java.util.Calendar.MONTH)
                                val quarterStartMonth = (currentMonth / 3) * 3
                                set(java.util.Calendar.MONTH, quarterStartMonth)
                                set(java.util.Calendar.DAY_OF_MONTH, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            java.util.Calendar.DAY_OF_YEAR -> {
                                set(java.util.Calendar.DAY_OF_YEAR, 1)
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                        }
                    }.timeInMillis
                )
            }

            grouped.getOrPut(truncatedTimestamp) { mutableListOf() }.add(score.value)
        }

        return grouped.map { (timestamp, values) ->
            Score(timestamp, values.average())
        }.sortedByDescending { it.timestamp }
    }

    private fun isHabitDueForDate(habit: Habit, entry: Entry): Boolean {
        return if (habit.isNumerical) {
            true
        } else {
            entry.value != Entry.YES_AUTO
        }
    }

    private fun isHabitCompletedForDate(habit: Habit, entry: Entry): Boolean {
        if (entry.value == Entry.SKIP) {
            return true
        }

        return if (habit.isNumerical) {
            if (entry.value == Entry.UNKNOWN) {
                false
            } else {
                val value = entry.value / 1000.0
                when (habit.targetType) {
                    NumericalHabitType.AT_LEAST -> value >= habit.targetValue
                    NumericalHabitType.AT_MOST -> value <= habit.targetValue
                }
            }
        } else {
            entry.value == Entry.YES_MANUAL
        }
    }
}

