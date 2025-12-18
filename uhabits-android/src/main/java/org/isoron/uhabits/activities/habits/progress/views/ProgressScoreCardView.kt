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
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.databinding.ProgressScoreCardBinding

class ProgressScoreCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    data class State(
        val scores: List<Score>,
        val bucketSize: Int,
        val spinnerPosition: Int,
        val color: PaletteColor,
        val theme: Theme
    )

    private var binding = ProgressScoreCardBinding.inflate(LayoutInflater.from(context), this)
    private var onSpinnerPositionChanged: ((Int) -> Unit)? = null

    fun setState(state: State) {
        val androidColor = state.theme.color(state.color).toInt()
        binding.title.setTextColor(androidColor)
        binding.spinner.setSelection(state.spinnerPosition)
        binding.scoreView.setScores(state.scores)
        binding.scoreView.reset()
        binding.scoreView.setBucketSize(state.bucketSize)
        binding.scoreView.setColor(androidColor)
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