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
import android.widget.LinearLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.databinding.ProgressSummaryWidgetBinding
import org.isoron.uhabits.utils.StyledResources
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

        // Theme-aware colors: use primary for gains, contrast40 for drops, contrast60 when flat
        val res = StyledResources(context)
        val positive = res.getColor(R.attr.colorPrimary)
        val negative = res.getColor(R.attr.contrast40)
        val neutral = res.getColor(R.attr.contrast60)
        val colorInt = when {
            change > 0.0001 -> positive
            change < -0.0001 -> negative
            else -> neutral
        }
        binding.changeText.setTextColor(colorInt)
    }
    
    fun setOnDetailsClickListener(listener: () -> Unit) {
        binding.progressWidgetRoot.setOnClickListener { listener() }
    }
}
