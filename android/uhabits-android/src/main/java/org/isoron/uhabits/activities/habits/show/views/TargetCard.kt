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
import android.content.res.*
import android.util.*
import android.view.*
import android.widget.*
import kotlinx.coroutines.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*
import java.util.*

data class TargetCardViewModel(
        val color: PaletteColor,
        val values: List<Double> = listOf(),
        val targets: List<Double> = listOf(),
        val labels: List<String> = listOf(),
)

class TargetCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val binding = ShowHabitTargetBinding.inflate(LayoutInflater.from(context), this)
    fun update(data: TargetCardViewModel) {
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.targetChart.setValues(data.values)
        binding.targetChart.setTargets(data.targets)
        binding.targetChart.setLabels(data.labels)
        binding.title.setTextColor(androidColor)
        binding.targetChart.setColor(androidColor)
        postInvalidate()
    }
}

class TargetCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
        val resources: Resources,
) {
    suspend fun present(): TargetCardViewModel = Dispatchers.IO {
        val checkmarks = habit.checkmarks
        val valueToday = checkmarks.todayValue / 1e3
        val valueThisWeek = checkmarks.getThisWeekValue(firstWeekday) / 1e3
        val valueThisMonth = checkmarks.thisMonthValue / 1e3
        val valueThisQuarter = checkmarks.thisQuarterValue / 1e3
        val valueThisYear = checkmarks.thisYearValue / 1e3

        val cal = DateUtils.getStartOfTodayCalendarWithOffset()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysInQuarter = 91
        val daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)

        val targetToday = habit.getTargetValue() / habit.frequency.denominator
        val targetThisWeek = targetToday * 7
        val targetThisMonth = targetToday * daysInMonth
        val targetThisQuarter = targetToday * daysInQuarter
        val targetThisYear = targetToday * daysInYear

        val values = ArrayList<Double>()
        if (habit.frequency.denominator <= 1) values.add(valueToday)
        if (habit.frequency.denominator <= 7) values.add(valueThisWeek)
        values.add(valueThisMonth)
        values.add(valueThisQuarter)
        values.add(valueThisYear)

        val targets = ArrayList<Double>()
        if (habit.frequency.denominator <= 1) targets.add(targetToday)
        if (habit.frequency.denominator <= 7) targets.add(targetThisWeek)
        targets.add(targetThisMonth)
        targets.add(targetThisQuarter)
        targets.add(targetThisYear)

        val labels = ArrayList<String>()
        if (habit.frequency.denominator <= 1) labels.add(resources.getString(R.string.today))
        if (habit.frequency.denominator <= 7) labels.add(resources.getString(R.string.week))
        labels.add(resources.getString(R.string.month))
        labels.add(resources.getString(R.string.quarter))
        labels.add(resources.getString(R.string.year))

        return@IO TargetCardViewModel(
                color = habit.color,
                values = values,
                labels = labels,
                targets = targets,
        )
    }
}
