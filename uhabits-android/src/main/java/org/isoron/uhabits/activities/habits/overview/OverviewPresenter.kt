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
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.overview.views.ImprovementStreakCardView
import org.isoron.uhabits.activities.habits.overview.views.OverviewScoreCardView
import org.isoron.uhabits.activities.habits.overview.views.OverviewStatsCardView
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.utils.DateUtils

class OverviewPresenter(
    private val context: Context,
    private val habitList: HabitList,
    private val calculator: AggregateScoreCalculator = AggregateScoreCalculator()
) {

    fun buildState(
        timeRangeDays: Int
    ): OverviewState {
        val matcher = HabitMatcher(isArchivedAllowed = false)
        val activeHabits = habitList.getFiltered(matcher)

        if (activeHabits.isEmpty) {
            return OverviewState(
                statsCard = OverviewStatsCardView.State(0.0, 0.0),
                scoreCard = OverviewScoreCardView.State(emptyList()),
                streakCard = null,
                isEmpty = true
            )
        }

        val today = DateUtils.getToday()
        val fromDate = if (timeRangeDays == Int.MAX_VALUE) {
            calculator.findEarliestHabitDate(activeHabits.toList(), today.minus(365))
        } else {
            today.minus(timeRangeDays - 1)
        }

        val scores = calculator.computeAggregateScores(activeHabits.toList(), fromDate, today)

        if (scores.isEmpty()) {
            return OverviewState(
                statsCard = OverviewStatsCardView.State(0.0, 0.0),
                scoreCard = OverviewScoreCardView.State(emptyList()),
                streakCard = null,
                isEmpty = true
            )
        }

        // Build stats card state
        val scoreToday = scores.lastOrNull()?.value ?: 0.0
        val scoreYesterday = if (scores.size >= 2) scores[scores.size - 2].value else 0.0
        val statsCardState = OverviewStatsCardView.State(
            scoreYesterday = scoreYesterday * 100,
            scoreToday = scoreToday * 100,
            color = PaletteColor(11) // Blue
        )

        // Build score card state - pass original scores, chart will handle Y-axis scaling
        val scoreCardState = OverviewScoreCardView.State(
            scores = scores,
            selectedTimeRange = timeRangeDays,
            color = PaletteColor(11), // Blue
            minScore = scores.minOfOrNull { it.value } ?: 0.0,
            maxScore = scores.maxOfOrNull { it.value } ?: 1.0
        )

        // Build streak card state (only if we have enough data)
        val streakCardState = if (scores.size >= 7) {
            val currentStreak = calculator.calculateImprovementStreak(scores)
            val longestStreak = calculator.calculateLongestStreak(scores)
            
            // Calculate recent trend (average change over last 7 days)
            val recentTrend = if (scores.size >= 7) {
                val last7 = scores.takeLast(7)
                var totalChange = 0.0
                for (i in 1 until last7.size) {
                    totalChange += (last7[i].value - last7[i - 1].value)
                }
                totalChange / 6.0
            } else {
                0.0
            }

            val insight = calculator.generateStreakInsight(currentStreak, longestStreak, recentTrend)

            ImprovementStreakCardView.State(
                currentStreak = currentStreak,
                insightMessage = insight
            )
        } else {
            null
        }

        return OverviewState(
            statsCard = statsCardState,
            scoreCard = scoreCardState,
            streakCard = streakCardState,
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