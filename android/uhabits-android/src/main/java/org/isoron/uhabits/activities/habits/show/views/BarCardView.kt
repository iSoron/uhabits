/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.ui.screens.habits.show.views.BarCardViewModel
import org.isoron.uhabits.core.ui.views.BarChart
import org.isoron.uhabits.databinding.ShowHabitBarBinding
import org.isoron.uhabits.utils.toThemedAndroidColor
import java.util.Locale

class BarCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitBarBinding.inflate(LayoutInflater.from(context), this)
    var onNumericalSpinnerPosition: (position: Int) -> Unit = {}
    var onBoolSpinnerPosition: (position: Int) -> Unit = {}

    fun update(data: BarCardViewModel) {
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.chart.view = BarChart(data.theme, JavaLocalDateFormatter(Locale.US)).apply {
            series = mutableListOf(data.entries.map { it.value / 1000.0 })
            colors = mutableListOf(theme.color(data.color.paletteIndex))
            axis = data.entries.map { it.timestamp.toLocalDate() }
        }
        binding.chart.resetDataOffset()
        binding.chart.postInvalidate()

        binding.title.setTextColor(androidColor)
        if (data.isNumerical) {
            binding.boolSpinner.visibility = GONE
        } else {
            binding.numericalSpinner.visibility = GONE
        }

        binding.numericalSpinner.onItemSelectedListener = null
        binding.numericalSpinner.setSelection(data.numericalSpinnerPosition)
        binding.numericalSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onNumericalSpinnerPosition(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.boolSpinner.onItemSelectedListener = null
        binding.boolSpinner.setSelection(data.boolSpinnerPosition)
        binding.boolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onBoolSpinnerPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}
