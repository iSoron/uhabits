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

import android.content.Context
import org.isoron.platform.time.DayOfWeek
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.overview.views.OverviewBarCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewFrequencyCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewHistoryCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewScoreCardView
import org.isoron.uhabits.activities.habits.overview.views.OverviewStatsCardView
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardState
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import java.util.Calendar

class OverviewPresenter(
    private val context: Context,
    private val habitList: HabitList,
    private val theme: Theme,
    private val firstWeekday: Int,
    private val calculator: AggregateScoreCalculator = AggregateScoreCalculator()
) {

    fun buildState(
        scoreSpinnerPosition: Int = 1,
        barSpinnerPosition: Int = 0
    ): OverviewState {
        val matcher = HabitMatcher(isArchivedAllowed = false)
        val activeHabits = habitList.getFiltered(matcher)

        if (activeHabits.isEmpty) {
            return OverviewState(
                statsCard = OverviewStatsCardView.State(0.0, 0.0),
                scoreCard = null,
                barCard = null,
                historyCard = null,
                streakCard = null,
                frequencyCard = null,
                isEmpty = true
            )
        }

        val today = DateUtils.getToday()
        val earliestDate = calculator.findEarliestHabitDate(activeHabits.toList(), today)
        val fullHistoryScores = calculator.computeAggregateScores(activeHabits.toList(), earliestDate, today)

        if (fullHistoryScores.isEmpty()) {
            return OverviewState(
                statsCard = OverviewStatsCardView.State(0.0, 0.0),
                scoreCard = null,
                barCard = null,
                historyCard = null,
                streakCard = null,
                frequencyCard = null,
                isEmpty = true
            )
        }

        // Build stats card state
        val scoreToday = fullHistoryScores.lastOrNull()?.value ?: 0.0
        val scoreYesterday = if (fullHistoryScores.size >= 2) fullHistoryScores[fullHistoryScores.size - 2].value else 0.0
        val statsCardState = OverviewStatsCardView.State(
            scoreYesterday = scoreYesterday * 100,
            scoreToday = scoreToday * 100,
            color = PaletteColor(11) // Blue
        )

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
        
        val scoreCardState = org.isoron.uhabits.activities.habits.overview.views.OverviewScoreCardState(
            scores = groupedScores,
            bucketSize = scoreBucketSize,
            spinnerPosition = scoreSpinnerPosition,
            color = PaletteColor(11), // Blue
            theme = theme
        )

        // Build streak card state using same fullHistoryScores
        val streakCardState = if (fullHistoryScores.size >= 2) {
            val bestStreaks = calculator.calculateAggregateStreaks(fullHistoryScores)
            if (bestStreaks.isNotEmpty()) {
                StreakCardState(
                    color = PaletteColor(11), // Blue
                    bestStreaks = bestStreaks,
                    theme = theme
                )
            } else {
                null
            }
        } else {
            null
        }

        // Build history card state - show all historical data
        val historyScores = calculator.computeAggregateScores(activeHabits.toList(), earliestDate, today)
        val historyCardState = if (historyScores.isNotEmpty()) {
            OverviewHistoryCardState(
                scoreValues = historyScores.map { it.value },
                color = PaletteColor(11), // Blue
                firstWeekday = DayOfWeek.SUNDAY,
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
                OverviewBarCardState(
                    theme = theme,
                    spinnerPosition = barSpinnerPosition,
                    bucketSize = bucketSize,
                    color = PaletteColor(11), // Blue
                    entries = entries
                )
            } else {
                null
            }
        } else {
            null
        }

        // Build frequency card state - show last ~12 months
        val frequencyStartDate = today.minus(365)
        val frequencyData = calculator.computeAggregateWeekdayFrequency(
            activeHabits.toList(),
            frequencyStartDate,
            today
        )
        val frequencyCardState = if (frequencyData.isNotEmpty()) {
            OverviewFrequencyCardState(
                frequency = frequencyData,
                color = PaletteColor(11), // Blue
                firstWeekday = firstWeekday,
                theme = theme
            )
        } else {
            null
        }

        return OverviewState(
            statsCard = statsCardState,
            scoreCard = scoreCardState,
            barCard = barCardState,
            historyCard = historyCardState,
            streakCard = streakCardState,
            frequencyCard = frequencyCardState,
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