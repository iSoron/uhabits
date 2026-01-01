/*
 * Copyright (C) 2016-2025 A?linson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.isoron.uhabits.R
import org.isoron.uhabits.databinding.ProgressSummaryWidgetBinding
import org.isoron.uhabits.utils.StyledResources

class ProgressSummaryWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ProgressSummaryWidgetBinding

    init {
        binding = ProgressSummaryWidgetBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL
    }

    fun setSummaryData(
        todayScore: Double,
        yesterdayScore: Double,
        todayCompleted: Int,
        todayDue: Int,
        yesterdayCompleted: Int,
        yesterdayDue: Int,
        yesterdayStreakLength: Int,
        projectedStreakLength: Int,
        todayScoreRank: Int,
        todayProgressRank: Int,
        todayCompletionRank: Int
    ) {
        val yesterdayCompletionPercent = if (yesterdayDue > 0) {
            yesterdayCompleted.toDouble() / yesterdayDue * 100
        } else {
            0.0
        }
        val todayCompletionPercent = if (todayDue > 0) {
            todayCompleted.toDouble() / todayDue * 100
        } else {
            0.0
        }
        val yesterdayScorePercent = yesterdayScore * 100
        val todayScorePercent = todayScore * 100
        val progressDiffPercent = (todayScore - yesterdayScore) * 100

        binding.yesterdaySummary.text = formatSummary(
            prefix = "Y:",
            completed = yesterdayCompleted,
            due = yesterdayDue,
            completionPercent = yesterdayCompletionPercent,
            scorePercent = yesterdayScorePercent,
            streakText = "Streak $yesterdayStreakLength",
            completionRank = null,
            scoreRank = null,
            progressRank = null
        )
        val progressText = formatProgressText(progressDiffPercent)
        val todaySummary = formatSummary(
            prefix = "T:",
            completed = todayCompleted,
            due = todayDue,
            completionPercent = todayCompletionPercent,
            scorePercent = todayScorePercent,
            progressText = progressText,
            streakText = "Streak $yesterdayStreakLength->$projectedStreakLength",
            completionRank = todayCompletionRank,
            scoreRank = todayScoreRank,
            progressRank = todayProgressRank
        )
        binding.todaySummary.text = colorizeProgress(todaySummary, progressText, progressDiffPercent)
    }

    private fun formatSummary(
        prefix: String,
        completed: Int,
        due: Int,
        completionPercent: Double,
        scorePercent: Double,
        streakText: String,
        progressText: String? = null,
        completionRank: Int? = null,
        scoreRank: Int? = null,
        progressRank: Int? = null
    ): String {
        val completionText = String.format("%d/%d (%.1f%%)", completed, due, completionPercent)
        val completionRankText = if (completionRank != null && completionRank > 0) {
            " #$completionRank"
        } else {
            ""
        }
        val completionValue = "$completionText$completionRankText"
        val scoreRankText = if (scoreRank != null && scoreRank > 0) {
            " #$scoreRank"
        } else {
            ""
        }
        val scoreText = String.format("Score %.5f%%%s", scorePercent, scoreRankText)
        val progressRankText = if (progressRank != null && progressRank > 0) {
            " #$progressRank"
        } else {
            ""
        }
        val progressValueText = progressText?.let { "$it$progressRankText" }

        val prefixWithRank = "$prefix $completionValue"
        return listOfNotNull(prefixWithRank, scoreText, progressValueText, streakText)
            .joinToString(" | ")
    }

    private fun formatProgressText(progressPercent: Double): String {
        val formatted = when {
            progressPercent > 0 -> String.format("+%.5f%%", progressPercent)
            progressPercent < 0 -> String.format("%.5f%%", progressPercent)
            else -> "0.00000%"
        }
        return "Progress $formatted"
    }

    private fun colorizeProgress(
        summary: String,
        progressText: String,
        progressPercent: Double
    ): SpannableString {
        val spannable = SpannableString(summary)
        val start = summary.indexOf(progressText)
        if (start < 0) {
            return spannable
        }
        val numberStartInProgress = progressText.indexOf(' ') + 1
        val numberStart = start + numberStartInProgress
        val end = start + progressText.length
        val neutral = StyledResources(context).getColor(R.attr.contrast60)
        val positive = ContextCompat.getColor(context, R.color.green_500)
        val negative = ContextCompat.getColor(context, R.color.red_500)
        val colorInt = when {
            progressPercent > 0.0 -> positive
            progressPercent < 0.0 -> negative
            else -> neutral
        }
        spannable.setSpan(
            ForegroundColorSpan(colorInt),
            numberStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    fun setOnDetailsClickListener(listener: () -> Unit) {
        binding.progressWidgetRoot.setOnClickListener { listener() }
    }
}
