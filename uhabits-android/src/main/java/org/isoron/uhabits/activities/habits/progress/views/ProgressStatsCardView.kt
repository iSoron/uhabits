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

package org.isoron.uhabits.activities.habits.progress.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.activities.common.views.RingView
import org.isoron.uhabits.databinding.ProgressStatsCardBinding
import org.isoron.uhabits.utils.PaletteUtils

class ProgressStatsCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ProgressStatsCardBinding

    init {
        inflate(context, R.layout.progress_stats_card, this)
        binding = ProgressStatsCardBinding.bind(this)
        orientation = VERTICAL
    }

    fun setState(state: State) {
        // Update stats text with 5 decimal places
        binding.statsYesterday.text = String.format("%.5f%%", state.scoreYesterday)
        binding.statsToday.text = String.format("%.5f%%", state.scoreToday)
        
        val diff = state.scoreToday - state.scoreYesterday
        val diffText = when {
            diff > 0 -> String.format("+%.5f%%", diff)
            diff < 0 -> String.format("%.5f%%", diff)
            else -> "0.00000%"
        }
        binding.statsChange.text = diffText

        // Set color based on change
        val color = when {
            diff > 0 -> PaletteUtils.getAndroidTestColor(7) // Green
            diff < 0 -> PaletteUtils.getAndroidTestColor(2) // Orange  
            else -> binding.statsChange.currentTextColor
        }
        binding.statsChange.setTextColor(color)
    }

    data class State(
        val scoreYesterday: Double,
        val scoreToday: Double,
        val color: PaletteColor = PaletteColor(11) // Blue
    )
}