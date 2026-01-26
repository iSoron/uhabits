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
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.ui.views.BarChart
import org.isoron.uhabits.databinding.ProgressBarCardBinding
import java.util.Locale

class ProgressBarCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ProgressBarCardBinding.inflate(LayoutInflater.from(context), this)
    private var onSpinnerPositionChanged: ((Int) -> Unit)? = null

    fun setState(state: ProgressBarCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        binding.title.text = state.title
        binding.chart.view = BarChart(state.theme, JavaLocalDateFormatter(Locale.getDefault())).apply {
            // For overview, values are already in 0-1000 range (scores * 1000)
            // Divide by 10 to show as 0-100 percentage
            series = mutableListOf(state.entries.map { it.value / 10.0 })
            colors = mutableListOf(theme.color(state.color.paletteIndex))
            axis = state.entries.map { it.timestamp.toLocalDate() }
        }
        binding.chart.resetDataOffset()
        binding.chart.postInvalidate()

        binding.title.setTextColor(androidColor)
        binding.spinner.setSelection(state.spinnerPosition)
    }

    fun setOnSpinnerPositionChanged(listener: (Int) -> Unit) {
        this.onSpinnerPositionChanged = listener
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}
