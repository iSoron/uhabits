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
import org.isoron.uhabits.databinding.ProgressCompletionFrequencyCardBinding

class ProgressCompletionFrequencyCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ProgressCompletionFrequencyCardBinding.inflate(LayoutInflater.from(context), this)

    fun setState(state: ProgressCompletionFrequencyCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        binding.title.setTextColor(androidColor)
        binding.frequencyChart.setFrequency(state.frequency)
        binding.frequencyChart.setIsNumerical(true)
        binding.frequencyChart.setFirstWeekday(state.firstWeekday)
        binding.frequencyChart.setColor(androidColor)
    }
}
