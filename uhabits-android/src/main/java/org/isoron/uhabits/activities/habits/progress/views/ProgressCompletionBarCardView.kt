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

package org.isoron.uhabits.activities.habits.progress.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.ui.views.BarChart
import org.isoron.uhabits.databinding.ProgressCompletionBarCardBinding
import java.util.Locale

class ProgressCompletionBarCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ProgressCompletionBarCardBinding.inflate(LayoutInflater.from(context), this)

    fun setState(state: ProgressCompletionBarCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        binding.title.setTextColor(androidColor)
        binding.chart.view = BarChart(state.theme, JavaLocalDateFormatter(Locale.getDefault())).apply {
            series = mutableListOf(state.entries.map { it.value.toDouble() })
            colors = mutableListOf(theme.color(state.color.paletteIndex))
            axis = state.entries.map { it.timestamp.toLocalDate() }
        }
        binding.chart.resetDataOffset()
        binding.chart.postInvalidate()
    }
}
