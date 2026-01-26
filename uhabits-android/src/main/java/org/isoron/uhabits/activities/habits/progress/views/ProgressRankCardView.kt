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
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.ui.views.BarChart
import org.isoron.uhabits.databinding.ProgressRankCardBinding
import java.util.Locale
import kotlin.text.Regex

class ProgressRankCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ProgressRankCardBinding.inflate(LayoutInflater.from(context), this)

    fun setState(state: ProgressRankCardState) {
        binding.title.text = state.title
        val titleColor = state.titleColor ?: state.colors.firstOrNull()
        if (titleColor != null) {
            binding.title.setTextColor(state.theme.color(titleColor).toInt())
        }
        val subtitle = state.subtitle
        if (subtitle.isNullOrBlank()) {
            binding.subtitle.visibility = View.GONE
        } else {
            binding.subtitle.visibility = View.VISIBLE
            // For overall rank card with multiple colors, colorize only the metric names in the legend
            if (state.colors.size > 1 && subtitle.contains("/")) {
                // Extract metric names (before the parenthesis)
                val metricsPartMatch = Regex("^([^(]+)").find(subtitle)
                if (metricsPartMatch != null) {
                    val metricsPart = metricsPartMatch.groupValues[1].trim()
                    val spannableString = SpannableString(subtitle)
                    val metricNames = metricsPart.split("/").map { it.trim() }
                    var searchStartIndex = 0
                    metricNames.forEachIndexed { index, metricName ->
                        val partStart = subtitle.indexOf(metricName, searchStartIndex)
                        if (partStart >= 0 && index < state.colors.size) {
                            val color = state.theme.color(state.colors[index]).toInt()
                            spannableString.setSpan(
                                ForegroundColorSpan(color),
                                partStart,
                                partStart + metricName.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            searchStartIndex = partStart + metricName.length
                        }
                    }
                    binding.subtitle.text = spannableString
                } else {
                    binding.subtitle.text = subtitle
                }
            } else {
                binding.subtitle.text = subtitle
            }
        }

        binding.chart.view = BarChart(state.theme, JavaLocalDateFormatter(Locale.getDefault())).apply {
            series = state.series.map { it.toList() }.toMutableList()
            colors = state.colors.map { state.theme.color(it) }.toMutableList()
            axis = state.axis
        }
        binding.chart.resetDataOffset()
        binding.chart.postInvalidate()
    }
}
