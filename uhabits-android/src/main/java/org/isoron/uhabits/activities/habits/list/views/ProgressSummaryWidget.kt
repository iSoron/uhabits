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
package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.RingView
import org.isoron.uhabits.databinding.ProgressSummaryWidgetBinding
import org.isoron.uhabits.utils.InterfaceUtils

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
        yesterdayStreakLength: Int,
        projectedStreakLength: Int,
        todayScoreRank: Int,
        todayProgressRank: Int,
        todayCompletionRank: Int,
        todayScoreRankTotal: Int,
        todayProgressRankTotal: Int,
        todayCompletionRankTotal: Int,
        todayStreakRank: Int,
        todayStreakRankTotal: Int,
        maxAbsProgressChange: Double
    ) {
        val todayCompletionPercent = if (todayDue > 0) {
            todayCompleted.toDouble() / todayDue * 100
        } else {
            0.0
        }
        val todayScorePercent = todayScore * 100
        val progressDiffPercent = (todayScore - yesterdayScore) * 100

        binding.progressValue.text = formatSignedPercent(progressDiffPercent)
        binding.progressRank.text = formatRank(todayProgressRank, todayProgressRankTotal)
        binding.streakValue.text = String.format("%d -> %dd", yesterdayStreakLength, projectedStreakLength)
        binding.streakRank.text = formatRank(todayStreakRank, todayStreakRankTotal)
        binding.scoreValue.text = String.format("%.5f%%", todayScorePercent)
        binding.scoreRank.text = formatRank(todayScoreRank, todayScoreRankTotal)
        binding.completionValue.text = String.format(
            "%d/%d (%.2f%%)",
            todayCompleted,
            todayDue,
            todayCompletionPercent
        )
        binding.completionRank.text = formatRank(todayCompletionRank, todayCompletionRankTotal)

        val positive = ContextCompat.getColor(context, R.color.light_green_600)
        val negative = ContextCompat.getColor(context, R.color.red_700)
        updateProgressFill(progressDiffPercent, maxAbsProgressChange, positive, negative)

        val scoreColor = ContextCompat.getColor(context, R.color.amber_800)
        val completionColor = ContextCompat.getColor(context, R.color.purple_600)
        updateRing(binding.scoreRing, todayScore.coerceIn(0.0, 1.0).toFloat(), scoreColor)
        updateRing(
            binding.completionRing,
            (todayCompletionPercent / 100.0).coerceIn(0.0, 1.0).toFloat(),
            completionColor
        )
    }

    private fun formatRank(rank: Int, total: Int): String {
        return if (rank > 0 && total > 0) {
            "$rank/$total"
        } else {
            "--"
        }
    }

    private fun formatSignedPercent(value: Double): String {
        return when {
            value > 0 -> String.format("+%.5f%%", value)
            value < 0 -> String.format("%.5f%%", value)
            else -> "0.00000%"
        }
    }

    private fun updateRing(ring: RingView, percent: Float, color: Int) {
        ring.setColor(color)
        ring.setPercentage(percent)
        ring.setPrecision(1f / 16f)
        ring.setThickness(InterfaceUtils.dpToPixels(context, 3f))
        ring.invalidate()
    }

    private fun updateProgressFill(
        progressPercent: Double,
        maxAbsProgressChange: Double,
        positiveColor: Int,
        negativeColor: Int
    ) {
        val fill = binding.progressFill
        val container = binding.progressVisual
        val maxAbs = if (maxAbsProgressChange > 0.0) maxAbsProgressChange else 1.0
        val clamped = progressPercent.coerceIn(-maxAbs, maxAbs)
        val containerWidth = container.width
        if (containerWidth == 0) {
            container.post { updateProgressFill(progressPercent, maxAbsProgressChange, positiveColor, negativeColor) }
            return
        }

        val half = containerWidth / 2f
        val ratio = kotlin.math.abs(clamped) / maxAbs
        val fillWidth = (half * ratio).toInt()
        val params = fill.layoutParams as ViewGroup.LayoutParams
        params.width = fillWidth
        fill.layoutParams = params
        fill.background = createFillDrawable(
            if (clamped >= 0) positiveColor else negativeColor,
            roundStart = clamped < 0,
            roundEnd = clamped >= 0
        )
        fill.translationX = if (clamped >= 0) half else half - fillWidth
    }

    private fun createFillDrawable(color: Int, roundStart: Boolean, roundEnd: Boolean): GradientDrawable {
        val radius = InterfaceUtils.dpToPixels(context, 4f).toFloat()
        val radii = floatArrayOf(
            if (roundStart) radius else 0f,
            if (roundStart) radius else 0f,
            if (roundEnd) radius else 0f,
            if (roundEnd) radius else 0f,
            if (roundEnd) radius else 0f,
            if (roundEnd) radius else 0f,
            if (roundStart) radius else 0f,
            if (roundStart) radius else 0f
        )
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radii
            setColor(color)
        }
    }

    fun setOnDetailsClickListener(listener: () -> Unit) {
        binding.progressWidgetRoot.setOnClickListener { listener() }
    }
}
