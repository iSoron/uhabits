/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardState
import org.isoron.uhabits.databinding.ShowHabitOverviewBinding
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.abs

class OverviewCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitOverviewBinding.inflate(LayoutInflater.from(context), this)

    private fun formatPercentageDiff(percentageDiff: Float): String {
        return String.format(
            "%s%.0f%%",
            if (percentageDiff >= 0) "+" else "\u2212",
            abs(percentageDiff) * 100
        )
    }

    fun setState(state: OverviewCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        val res = StyledResources(context)
        val inactiveColor = res.getColor(R.attr.contrast60)
        binding.monthDiffLabel.setTextColor(if (state.scoreMonthDiff >= 0) androidColor else inactiveColor)
        binding.monthDiffLabel.text = formatPercentageDiff(state.scoreMonthDiff)
        binding.scoreLabel.setTextColor(androidColor)
        binding.scoreLabel.text = String.format("%.0f%%", state.scoreToday * 100)
        binding.scoreRing.setColor(androidColor)
        binding.scoreRing.setPercentage(state.scoreToday)

        binding.title.setTextColor(androidColor)
        binding.totalCountLabel.setTextColor(androidColor)
        binding.totalCountLabel.text = state.totalCount.toString()
        binding.yearDiffLabel.setTextColor(if (state.scoreYearDiff >= 0) androidColor else inactiveColor)
        binding.yearDiffLabel.text = formatPercentageDiff(state.scoreYearDiff)
        postInvalidate()
    }
}
