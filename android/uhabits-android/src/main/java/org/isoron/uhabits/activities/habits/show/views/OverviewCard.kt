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

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import kotlinx.coroutines.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class OverviewCardViewModel(
        val color: PaletteColor,
        val scoreMonthDiff: Float,
        val scoreYearDiff: Float,
        val scoreToday: Float,
        val totalCount: Long,
)

class OverviewCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitOverviewBinding.inflate(LayoutInflater.from(context), this)

    private fun formatPercentageDiff(percentageDiff: Float): String {
        return String.format("%s%.0f%%", if (percentageDiff >= 0) "+" else "\u2212",
                             Math.abs(percentageDiff) * 100)
    }

    fun update(data: OverviewCardViewModel) {
        val androidColor = data.color.toThemedAndroidColor(context)
        val res = StyledResources(context)
        val inactiveColor = res.getColor(R.attr.mediumContrastTextColor)
        binding.monthDiffLabel.setTextColor(if (data.scoreMonthDiff >= 0) androidColor else inactiveColor)
        binding.monthDiffLabel.text = formatPercentageDiff(data.scoreMonthDiff)
        binding.scoreLabel.setTextColor(androidColor)
        binding.scoreLabel.text = String.format("%.0f%%", data.scoreToday * 100)
        binding.scoreRing.color = androidColor
        binding.scoreRing.percentage = data.scoreToday
        binding.title.setTextColor(androidColor)
        binding.totalCountLabel.setTextColor(androidColor)
        binding.totalCountLabel.text = data.totalCount.toString()
        binding.yearDiffLabel.setTextColor(if (data.scoreYearDiff >= 0) androidColor else inactiveColor)
        binding.yearDiffLabel.text = formatPercentageDiff(data.scoreYearDiff)
        postInvalidate()
    }
}

class OverviewCardPresenter(val habit: Habit) {
    suspend fun present(): OverviewCardViewModel = Dispatchers.IO {
        val today = DateUtils.getTodayWithOffset()
        val lastMonth = today.minus(30)
        val lastYear = today.minus(365)
        val scores = habit.scores
        val scoreToday = scores.todayValue.toFloat()
        val scoreLastMonth = scores.getValue(lastMonth).toFloat()
        val scoreLastYear = scores.getValue(lastYear).toFloat()
        return@IO OverviewCardViewModel(
                color = habit.color,
                scoreToday = scoreToday,
                scoreMonthDiff = scoreToday - scoreLastMonth,
                scoreYearDiff = scoreToday - scoreLastYear,
                totalCount = habit.originalEntries.totalCount,
        )
    }
}