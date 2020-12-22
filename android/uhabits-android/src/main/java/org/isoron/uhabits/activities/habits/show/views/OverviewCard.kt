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
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.habits.show.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

class OverviewCard : LinearLayout, ShowHabitPresenter.Listener {

    private val binding = ShowHabitOverviewBinding.inflate(LayoutInflater.from(context), this)
    lateinit var presenter: ShowHabitPresenter

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        if (isInEditMode) initEditMode()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.addListener(this)
        presenter.requestData(this)
    }

    override fun onDetachedFromWindow() {
        presenter.removeListener(this)
        super.onDetachedFromWindow()
    }

    private fun initEditMode() {
        onData(ShowHabitViewModel(
                scoreToday = 0.6f,
                scoreMonthDiff = 0.42f,
                scoreYearDiff = 0.75f,
        ))
    }

    private fun formatPercentageDiff(percentageDiff: Float): String {
        return String.format("%s%.0f%%", if (percentageDiff >= 0) "+" else "\u2212",
                             Math.abs(percentageDiff) * 100)
    }

    override fun onData(data: ShowHabitViewModel) {
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