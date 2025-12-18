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

package org.isoron.uhabits.activities.habits.overview.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.ScoreChart
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.databinding.OverviewScoreCardBinding
import org.isoron.uhabits.utils.PaletteUtils

class OverviewScoreCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: OverviewScoreCardBinding
    var onTimeRangeChanged: ((Int) -> Unit)? = null

    init {
        inflate(context, R.layout.overview_score_card, this)
        binding = OverviewScoreCardBinding.bind(this)
        orientation = VERTICAL

        setupTimeRangeSpinner()
    }

    private fun setupTimeRangeSpinner() {
        val timeRanges = arrayOf(
            context.getString(R.string.last_7_days),
            context.getString(R.string.last_30_days),
            context.getString(R.string.last_60_days),
            context.getString(R.string.last_90_days),
            context.getString(R.string.last_180_days),
            context.getString(R.string.last_365_days),
            context.getString(R.string.all_time)
        )

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            timeRanges
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.timeRangeSpinner.adapter = adapter

        binding.timeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val days = when (position) {
                    0 -> 7
                    1 -> 30
                    2 -> 60
                    3 -> 90
                    4 -> 180
                    5 -> 365
                    6 -> Int.MAX_VALUE // All time
                    else -> 30
                }
                onTimeRangeChanged?.invoke(days)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun setState(state: State) {
        binding.scoreChart.setScores(state.scores.reversed()) // ScoreChart expects reversed order
        binding.scoreChart.setColor(PaletteUtils.getAndroidTestColor(state.color.paletteIndex))
        
        // Set dynamic Y-axis bounds: min-2% to max+2%
        val minBound = (state.minScore - 0.02).coerceAtLeast(0.0)
        val maxBound = (state.maxScore + 0.02).coerceAtMost(1.0)
        binding.scoreChart.setYAxisBounds(minBound, maxBound)
        
        // Set bucket size based on time range for proper X-axis scaling
        val bucketSize = when {
            state.selectedTimeRange <= 7 -> 1  // Daily granularity for 7 days
            state.selectedTimeRange <= 60 -> 1  // Daily for up to 60 days
            state.selectedTimeRange <= 180 -> 7  // Weekly for up to 180 days
            else -> 7  // Weekly for longer periods
        }
        binding.scoreChart.setBucketSize(bucketSize)
        
        // Set spinner selection without triggering listener
        val position = when (state.selectedTimeRange) {
            7 -> 0
            30 -> 1
            60 -> 2
            90 -> 3
            180 -> 4
            365 -> 5
            Int.MAX_VALUE -> 6
            else -> 0 // Default to 7 days
        }
        binding.timeRangeSpinner.setSelection(position)
    }

    data class State(
        val scores: List<Score>,
        val selectedTimeRange: Int = 7,
        val color: PaletteColor = PaletteColor(11), // Blue
        val minScore: Double = 0.0,
        val maxScore: Double = 1.0
    )
}