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
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.databinding.ProgressCompletionStatsCardBinding
import org.isoron.uhabits.utils.PaletteUtils

class ProgressCompletionStatsCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ProgressCompletionStatsCardBinding

    init {
        inflate(context, R.layout.progress_completion_stats_card, this)
        binding = ProgressCompletionStatsCardBinding.bind(this)
        orientation = VERTICAL
    }

    fun setState(state: State) {
        binding.title.setTextColor(state.theme.color(state.color).toInt())

        val yesterdayPercent = if (state.yesterdayDue > 0) {
            state.yesterdayCompleted.toDouble() / state.yesterdayDue * 100
        } else {
            0.0
        }
        val todayPercent = if (state.todayDue > 0) {
            state.todayCompleted.toDouble() / state.todayDue * 100
        } else {
            0.0
        }

        binding.statsYesterday.text = formatCompletion(state.yesterdayCompleted, state.yesterdayDue, yesterdayPercent)
        binding.statsToday.text = formatCompletion(state.todayCompleted, state.todayDue, todayPercent)

        val diff = todayPercent - yesterdayPercent
        val diffText = when {
            diff > 0 -> String.format("+%.1f%%", diff)
            diff < 0 -> String.format("%.1f%%", diff)
            else -> "0.0%"
        }
        binding.statsChange.text = diffText

        val color = when {
            diff > 0 -> PaletteUtils.getAndroidTestColor(7)
            diff < 0 -> PaletteUtils.getAndroidTestColor(2)
            else -> binding.statsChange.currentTextColor
        }
        binding.statsChange.setTextColor(color)
    }

    private fun formatCompletion(completed: Int, due: Int, percent: Double): String {
        return String.format("%d/%d (%.1f%%)", completed, due, percent)
    }

    data class State(
        val yesterdayCompleted: Int,
        val yesterdayDue: Int,
        val todayCompleted: Int,
        val todayDue: Int,
        val color: PaletteColor = PaletteColor(14),
        val theme: Theme
    )
}
