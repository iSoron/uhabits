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

import android.content.Context
import org.isoron.platform.gui.Color
import org.isoron.platform.time.DayOfWeek
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.progress.views.ProgressBarCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionBarCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionFrequencyCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionHistoryCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionStatsCardView
import org.isoron.uhabits.activities.habits.progress.views.ProgressDeltaCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressFrequencyCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressHistoryCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressRankCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressScoreCardView
import org.isoron.uhabits.activities.habits.progress.views.ProgressStatsCardView
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardState
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.utils.PaletteUtils
import java.util.Calendar

class ProgressPresenter(
    private val context: Context,
    private val habitList: HabitList,
    private val theme: Theme,
    private val firstWeekday: Int,
    private val calculator: AggregateScoreCalculator = AggregateScoreCalculator()
) {

    fun buildState(
        scoreSpinnerPosition: Int = 1,
        barSpinnerPosition: Int = 0,
        deltaSpinnerPosition: Int = 0
    ): ProgressState {
        val matcher = HabitMatcher(isArchivedAllowed = false)
        val activeHabits = habitList.getFiltered(matcher)

        if (activeHabits.isEmpty) {
            return ProgressState(
                statsCard = ProgressStatsCardView.State(
                    scoreYesterday = 0.0,
                    scoreToday = 0.0,
                    color = ProgressSectionColors.score,
                    theme = theme
                ),
                scoreCard = null,
                barCard = null,
                deltaCard = null,
                historyCard = null,
                streakCard = null,
                frequencyCard = null,
                completionStatsCard = null,
                completionBarCard = null,
                completionHistoryCard = null,
                completionStreakCard = null,
                completionFrequencyCard = null,
                scoreRankCard = null,
                progressRankCard = null,
                streakRankCard = null,
                completionRankCard = null,
                overallRankCard = null,
                isEmpty = true
            )
        }

        // Use getTodayWithOffset to respect the "day closure" (3 AM) setting
        val today = DateUtils.getTodayWithOffset()
        val earliestDate = calculator.findEarliestHabitDate(activeHabits.toList(), today)
        val fullHistoryScores = calculator.computeAggregateScores(activeHabits.toList(), earliestDate, today)

        if (fullHistoryScores.isEmpty()) {
            return ProgressState(
                statsCard = ProgressStatsCardView.State(
                    scoreYesterday = 0.0,
                    scoreToday = 0.0,
                    color = ProgressSectionColors.score,
                    theme = theme
                ),
                scoreCard = null,
                barCard = null,
                deltaCard = null,
                historyCard = null,
                streakCard = null,
                frequencyCard = null,
                completionStatsCard = null,
                completionBarCard = null,
                completionHistoryCard = null,
                completionStreakCard = null,
                completionFrequencyCard = null,
                scoreRankCard = null,
                progressRankCard = null,
                streakRankCard = null,
                completionRankCard = null,
                overallRankCard = null,
                isEmpty = true
            )
        }

        // Build stats card state
        val scoreToday = fullHistoryScores.lastOrNull()?.value ?: 0.0
        val scoreYesterday = if (fullHistoryScores.size >= 2) fullHistoryScores[fullHistoryScores.size - 2].value else 0.0
        val statsCardState = ProgressStatsCardView.State(
            scoreYesterday = scoreYesterday * 100,
            scoreToday = scoreToday * 100,
            color = ProgressSectionColors.progress,
            theme = theme
        )

        val completionSummaries = calculator.computeAggregateCompletionSummaries(
            activeHabits.toList(),
            earliestDate,
            today
        )
        val completionToday = completionSummaries.lastOrNull()
        val completionYesterday = if (completionSummaries.size >= 2) {
            completionSummaries[completionSummaries.size - 2]
        } else {
            completionToday
        }
        val completionStatsCardState = if (completionToday != null && completionYesterday != null) {
            ProgressCompletionStatsCardView.State(
                yesterdayCompleted = completionYesterday.completedCount,
                yesterdayDue = completionYesterday.dueCount,
                todayCompleted = completionToday.completedCount,
                todayDue = completionToday.dueCount,
                color = ProgressSectionColors.completion,
                theme = theme
            )
        } else {
            null
        }

        // Build score card state - use same logic as individual habit ScoreCardPresenter
        val scoreBucketSizes = intArrayOf(1, 7, 31, 92, 365)
        val scoreBucketSize = scoreBucketSizes[scoreSpinnerPosition]
        val scoreTruncateField = when (scoreBucketSize) {
            1 -> DateUtils.TruncateField.DAY
            7 -> DateUtils.TruncateField.WEEK_NUMBER
            31 -> DateUtils.TruncateField.MONTH
            92 -> DateUtils.TruncateField.QUARTER
            365 -> DateUtils.TruncateField.YEAR
            else -> DateUtils.TruncateField.MONTH
        }

        val groupedScores = fullHistoryScores.groupBy {
            DateUtils.truncate(scoreTruncateField, it.timestamp, firstWeekday)
        }.map { (timestamp, scores) ->
            org.isoron.uhabits.core.models.Score(
                timestamp,
                scores.map { it.value }.average()
            )
        }.sortedBy { it.timestamp }.reversed()

        val scoreCardState = org.isoron.uhabits.activities.habits.progress.views.ProgressScoreCardView.State(
            scores = groupedScores,
            bucketSize = scoreBucketSize,
            spinnerPosition = scoreSpinnerPosition,
            color = ProgressSectionColors.score,
            theme = theme
        )

        // Build streak card state using same fullHistoryScores
        val streakCardState = if (fullHistoryScores.size >= 2) {
            val bestStreaks = calculator.calculateAggregateStreaks(fullHistoryScores)
            if (bestStreaks.isNotEmpty()) {
                StreakCardState(
                    color = ProgressSectionColors.streak,
                    bestStreaks = bestStreaks,
                    theme = theme
                )
            } else {
                null
            }
        } else {
            null
        }

        // Build progress-change calendar card state
        val progressChanges = calculator.computeAggregateProgressChanges(
            activeHabits.toList(),
            earliestDate,
            today
        )
        val historyFirstWeekday = DayOfWeek.values().getOrNull((firstWeekday - 1).coerceIn(0, 6)) ?: DayOfWeek.MONDAY
        val maxAbsChange = progressChanges.maxOfOrNull { kotlin.math.abs(it.value) } ?: 0.0
        val historyCardState = if (progressChanges.isNotEmpty()) {
            ProgressHistoryCardState(
                scoreValues = progressChanges.map {
                    if (maxAbsChange > 0) it.value / maxAbsChange else 0.0
                }.reversed(),
                color = ProgressSectionColors.progress,
                negativeColor = ProgressSectionColors.negative,
                firstWeekday = historyFirstWeekday,
                theme = theme,
                today = today.toLocalDate(),
                bipolarMode = true
            )
        } else {
            null
        }

        val completionHistoryCardState = if (completionSummaries.isNotEmpty()) {
            ProgressCompletionHistoryCardState(
                completionRatios = completionSummaries.map { it.completionRatio }.reversed(),
                color = ProgressSectionColors.completion,
                firstWeekday = historyFirstWeekday,
                theme = theme,
                today = today.toLocalDate()
            )
        } else {
            null
        }

        // Build bar card state - show average scores by period (Day/Week/Month/Quarter/Year)
        val bucketSizes = intArrayOf(1, 7, 31, 92, 365)
        val barCardState = if (barSpinnerPosition in bucketSizes.indices) {
            val bucketSize = bucketSizes[barSpinnerPosition]
            val barStartDate = earliestDate // Show all historical data

            // For daily view, don't group - show each individual day
            // For other views, use truncation
            val truncateField = when (bucketSize) {
                1 -> -1 // Daily - no truncation, each day is separate
                7 -> Calendar.DAY_OF_WEEK
                31 -> Calendar.DAY_OF_MONTH
                92 -> Calendar.MONTH // For quarters, truncate by month then group by 3
                365 -> Calendar.DAY_OF_YEAR
                else -> Calendar.DAY_OF_WEEK
            }

            val entries = calculator.computeAggregateEntriesByPeriod(
                activeHabits.toList(),
                barStartDate,
                today,
                bucketSize,
                truncateField
            )
            if (entries.isNotEmpty()) {
                ProgressBarCardState(
                    theme = theme,
                    spinnerPosition = barSpinnerPosition,
                    bucketSize = bucketSize,
                    color = ProgressSectionColors.score,
                    entries = entries,
                    title = context.getString(R.string.day_score_history_title)
                )
            } else {
                null
            }
        } else {
            null
        }

        val completionEntries = calculator.computeCompletionEntriesByPeriod(
            activeHabits.toList(),
            earliestDate,
            today,
            -1
        )
        val completionBarCardState = if (completionEntries.isNotEmpty()) {
            ProgressCompletionBarCardState(
                theme = theme,
                color = ProgressSectionColors.completion,
                entries = completionEntries
            )
        } else {
            null
        }

        // Build delta card state - score changes vs previous period
        val deltaBucketSizes = intArrayOf(1, 7, 31, 92, 365)
        val deltaCardState = if (deltaSpinnerPosition in deltaBucketSizes.indices) {
            val bucketSize = deltaBucketSizes[deltaSpinnerPosition]
            val truncateField = when (bucketSize) {
                1 -> -1
                7 -> Calendar.DAY_OF_WEEK
                31 -> Calendar.DAY_OF_MONTH
                92 -> Calendar.MONTH
                365 -> Calendar.DAY_OF_YEAR
                else -> Calendar.DAY_OF_WEEK
            }
            val periodScores = calculator.computeAggregateScoresByPeriod(
                activeHabits.toList(),
                earliestDate,
                today,
                bucketSize,
                truncateField
            )
            val deltas = if (periodScores.size >= 2) {
                periodScores.zipWithNext { current, previous ->
                    Score(current.timestamp, (current.value - previous.value) * 100)
                }
            } else {
                emptyList()
            }
            ProgressDeltaCardState(
                theme = theme,
                spinnerPosition = deltaSpinnerPosition,
                bucketSize = bucketSize,
                color = ProgressSectionColors.progress,
                positiveColor = Color(PaletteUtils.getAndroidTestColor(ProgressSectionColors.positive.paletteIndex)),
                negativeColor = Color(PaletteUtils.getAndroidTestColor(ProgressSectionColors.negative.paletteIndex)),
                deltas = deltas,
                emptyMessage = if (deltas.isEmpty()) {
                    context.getString(R.string.progress_change_not_enough_data)
                } else {
                    null
                }
            )
        } else {
            null
        }

        // Build frequency card state - show last ~12 months
        val frequencyStartDate = today.minus(365)
        val frequencyData = calculator.computeAggregateProgressChangeWeekdayFrequency(
            activeHabits.toList(),
            frequencyStartDate,
            today
        )
        val frequencyCardState = if (frequencyData.isNotEmpty()) {
            ProgressFrequencyCardState(
                frequency = frequencyData,
                color = ProgressSectionColors.progress,
                negativeColor = ProgressSectionColors.negative,
                firstWeekday = firstWeekday,
                theme = theme
            )
        } else {
            null
        }

        val completionFrequencyData = calculator.computeCompletionWeekdayFrequency(
            activeHabits.toList(),
            frequencyStartDate,
            today
        )
        val completionFrequencyCardState = if (completionFrequencyData.isNotEmpty()) {
            ProgressCompletionFrequencyCardState(
                frequency = completionFrequencyData,
                color = ProgressSectionColors.completion,
                firstWeekday = firstWeekday,
                theme = theme
            )
        } else {
            null
        }

        val completionStreakStart = today.minus(150)
        val completionStreakSummaries = completionSummaries.filter {
            !it.timestamp.isOlderThan(completionStreakStart)
        }
        val completionStreaks = calculator.calculateCompletionStreaks(completionStreakSummaries)
        val completionStreakCardState = StreakCardState(
            color = ProgressSectionColors.completion,
            bestStreaks = completionStreaks,
            theme = theme
        )

        val scoreHistory = fullHistoryScores.reversed()
        val progressHistory = progressChanges.reversed()
        val completionHistory = completionSummaries.reversed()
        val improvementStreaks = calculator.computeImprovementStreakLengths(fullHistoryScores)
        val streakHistory = improvementStreaks.reversed()

        val scoreRanks = calculator.computeDescendingRanks(
            scoreHistory.map { it.timestamp to it.value }
        )
        val progressRanks = calculator.computeDescendingRanks(
            progressHistory.map { it.timestamp to it.value }
        )
        val completionRanks = calculator.computeDescendingRanks(
            completionHistory.map { summary ->
                summary.timestamp to if (summary.dueCount > 0) summary.completionRatio else null
            }
        )
        val streakRanks = calculator.computeDescendingRanks(
            streakHistory.map { it.timestamp to it.value }
        )

        val scoreRankValues = scoreHistory.map { (scoreRanks[it.timestamp] ?: 0).toDouble() }
        val progressRankValues = progressHistory.map { (progressRanks[it.timestamp] ?: 0).toDouble() }
        val completionRankValues = completionHistory.map { (completionRanks[it.timestamp] ?: 0).toDouble() }
        val streakRankValues = streakHistory.map { (streakRanks[it.timestamp] ?: 0).toDouble() }

        val scoreRankCardState = if (scoreRankValues.any { it > 0 }) {
            ProgressRankCardState(
                title = context.getString(R.string.rank_score_title),
                subtitle = context.getString(R.string.rank_one_best),
                axis = scoreHistory.map { it.timestamp.toLocalDate() },
                series = listOf(scoreRankValues),
                colors = listOf(ProgressSectionColors.score),
                titleColor = ProgressSectionColors.score,
                theme = theme
            )
        } else {
            null
        }

        val progressRankCardState = if (progressRankValues.any { it > 0 }) {
            ProgressRankCardState(
                title = context.getString(R.string.rank_progress_title),
                subtitle = context.getString(R.string.rank_one_best),
                axis = progressHistory.map { it.timestamp.toLocalDate() },
                series = listOf(progressRankValues),
                colors = listOf(ProgressSectionColors.progress),
                titleColor = ProgressSectionColors.progress,
                theme = theme
            )
        } else {
            null
        }

        val streakRankCardState = if (streakRankValues.any { it > 0 }) {
            ProgressRankCardState(
                title = context.getString(R.string.rank_streak_title),
                subtitle = context.getString(R.string.rank_one_best),
                axis = streakHistory.map { it.timestamp.toLocalDate() },
                series = listOf(streakRankValues),
                colors = listOf(ProgressSectionColors.streak),
                titleColor = ProgressSectionColors.streak,
                theme = theme
            )
        } else {
            null
        }

        val completionRankCardState = if (completionRankValues.any { it > 0 }) {
            ProgressRankCardState(
                title = context.getString(R.string.rank_completion_title),
                subtitle = context.getString(R.string.rank_one_best),
                axis = completionHistory.map { it.timestamp.toLocalDate() },
                series = listOf(completionRankValues),
                colors = listOf(ProgressSectionColors.completion),
                titleColor = ProgressSectionColors.completion,
                theme = theme
            )
        } else {
            null
        }

        val overallRankCardState = if (
            scoreRankValues.any { it > 0 } ||
            progressRankValues.any { it > 0 } ||
            completionRankValues.any { it > 0 }
        ) {
            ProgressRankCardState(
                title = context.getString(R.string.rank_overall_title),
                subtitle = context.getString(R.string.rank_overall_subtitle),
                axis = scoreHistory.map { it.timestamp.toLocalDate() },
                series = listOf(scoreRankValues, progressRankValues, completionRankValues),
                colors = listOf(
                    ProgressSectionColors.score,
                    ProgressSectionColors.progress,
                    ProgressSectionColors.completion
                ),
                titleColor = ProgressSectionColors.overall,
                theme = theme
            )
        } else {
            null
        }

        return ProgressState(
            statsCard = statsCardState,
            scoreCard = scoreCardState,
            barCard = barCardState,
            deltaCard = deltaCardState,
            historyCard = historyCardState,
            streakCard = streakCardState,
            frequencyCard = frequencyCardState,
            completionStatsCard = completionStatsCardState,
            completionBarCard = completionBarCardState,
            completionHistoryCard = completionHistoryCardState,
            completionStreakCard = completionStreakCardState,
            completionFrequencyCard = completionFrequencyCardState,
            scoreRankCard = scoreRankCardState,
            progressRankCard = progressRankCardState,
            streakRankCard = streakRankCardState,
            completionRankCard = completionRankCardState,
            overallRankCard = overallRankCardState,
            isEmpty = false
        )
    }

    /**
     * No longer used - ScoreChart now handles dynamic Y-axis bounds directly.
     * Kept for reference.
     */
    @Deprecated("ScoreChart now handles Y-axis scaling")
    private fun normalizeScoresForChart(scores: List<org.isoron.uhabits.core.models.Score>): List<org.isoron.uhabits.core.models.Score> {
        if (scores.isEmpty()) return scores

        val values = scores.map { it.value }
        val minValue = values.minOrNull() ?: 0.0
        val maxValue = values.maxOrNull() ?: 1.0

        // Add 2% padding to min and max
        val range = maxValue - minValue
        val paddedMin = (minValue - 0.02).coerceAtLeast(0.0)
        val paddedMax = (maxValue + 0.02).coerceAtMost(1.0)
        val paddedRange = paddedMax - paddedMin

        // Avoid division by zero
        if (paddedRange < 0.001) return scores

        // Normalize to 0-1 range based on padded min/max
        return scores.map { score ->
            val normalizedValue = ((score.value - paddedMin) / paddedRange).coerceIn(0.0, 1.0)
            org.isoron.uhabits.core.models.Score(score.timestamp, normalizedValue)
        }
    }
}
