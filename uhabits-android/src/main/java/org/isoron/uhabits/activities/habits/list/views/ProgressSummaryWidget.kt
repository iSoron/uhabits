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
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.databinding.ProgressSummaryWidgetBinding
import org.isoron.uhabits.utils.StyledResources
import androidx.core.content.ContextCompat
import java.text.DecimalFormat

class ProgressSummaryWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    
    private val binding: ProgressSummaryWidgetBinding
    
    private val decimalFormat = DecimalFormat("0.00000")
    
    init {
        binding = ProgressSummaryWidgetBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL
    }
    
    fun setProgress(todayScore: Double, yesterdayScore: Double) {
        val change = todayScore - yesterdayScore
        
        // Show progress value without label (label is separate in XML)
        binding.progressText.text = "${decimalFormat.format(todayScore * 100)}%"
        
        val changeFormatted = decimalFormat.format(change * 100)
        binding.changeText.text = when {
            change > 0 -> "+$changeFormatted%"
            change < 0 -> "$changeFormatted%"
            else -> "±0.00000%"
        }

        // Positive -> green, Negative -> red, Neutral -> theme contrast
        val neutral = StyledResources(context).getColor(R.attr.contrast60)
        val positive = ContextCompat.getColor(context, org.isoron.uhabits.R.color.green_500)
        val negative = ContextCompat.getColor(context, org.isoron.uhabits.R.color.red_500)
        val colorInt = if (change > 0.0) positive else if (change < 0.0) negative else neutral
        binding.changeText.setTextColor(colorInt)
    }
    
    /**
     * Sets streak information in the widget.
     * @param currentStreakLength Length of the current active streak (0 if no active streak)
     * @param bestStreakLength Length of the best/longest streak (0 if no streaks)
     */
    fun setStreakData(currentStreakLength: Int, bestStreakLength: Int) {
        if (currentStreakLength == 0 && bestStreakLength == 0) {
            // Hide streak row if no streaks
            binding.streakRow.visibility = View.GONE
            return
        }
        
        binding.streakRow.visibility = View.VISIBLE
        
        // Show current streak
        val currentText = if (currentStreakLength > 0) {
            val days = if (currentStreakLength == 1) "day" else "days"
            "Current: $currentStreakLength $days"
        } else {
            "Current: —"
        }
        binding.streakText.text = currentText
        
        // Show best streak
        val bestText = if (bestStreakLength > 0) {
            "Best: $bestStreakLength"
        } else {
            "Best: —"
        }
        binding.bestStreakText.text = bestText
    }
    
    fun setOnDetailsClickListener(listener: () -> Unit) {
        binding.progressWidgetRoot.setOnClickListener { listener() }
    }
}
