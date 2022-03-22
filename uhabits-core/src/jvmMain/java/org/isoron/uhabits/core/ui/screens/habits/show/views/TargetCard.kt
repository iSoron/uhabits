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

package org.isoron.uhabits.core.ui.screens.habits.show.views

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.countSkippedDays
import org.isoron.uhabits.core.models.groupedSum
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import java.util.ArrayList
import java.util.Calendar

data class TargetCardState(
    val color: PaletteColor,
    val values: List<Double> = listOf(),
    val targets: List<Double> = listOf(),
    val intervals: List<Int> = listOf(),
    val theme: Theme,
)

class TargetCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            theme: Theme,
        ): TargetCardState {
            val today = DateUtils.getTodayWithOffset()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
            val entries = habit.computedEntries.getByInterval(oldest, today)

            val valueToday = entries.groupedSum(
                truncateField = DateUtils.TruncateField.DAY,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDayToday = entries.countSkippedDays(
                truncateField = DateUtils.TruncateField.DAY
            ).firstOrNull()?.value ?: 0

            val valueThisWeek = entries.groupedSum(
                truncateField = DateUtils.TruncateField.WEEK_NUMBER,
                firstWeekday = firstWeekday,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisWeek = entries.countSkippedDays(
                truncateField = DateUtils.TruncateField.WEEK_NUMBER,
                firstWeekday = firstWeekday
            ).firstOrNull()?.value ?: 0

            val valueThisMonth = entries.groupedSum(
                truncateField = DateUtils.TruncateField.MONTH,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisMonth = entries.countSkippedDays(
                truncateField = DateUtils.TruncateField.MONTH,
            ).firstOrNull()?.value ?: 0

            val valueThisQuarter = entries.groupedSum(
                truncateField = DateUtils.TruncateField.QUARTER,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisQuarter = entries.countSkippedDays(
                truncateField = DateUtils.TruncateField.QUARTER
            ).firstOrNull()?.value ?: 0

            val valueThisYear = entries.groupedSum(
                truncateField = DateUtils.TruncateField.YEAR,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisYear = entries.countSkippedDays(
                truncateField = DateUtils.TruncateField.YEAR
            ).firstOrNull()?.value ?: 0

            val cal = DateUtils.getStartOfTodayCalendarWithOffset()
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysInQuarter = 91
            val daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)

            val dailyTarget = habit.targetValue / habit.frequency.denominator
            val targetToday = dailyTarget * (1 - skippedDayToday)
            val targetThisWeek = dailyTarget * (7 - skippedDaysThisWeek)
            val targetThisMonth = dailyTarget * (daysInMonth - skippedDaysThisMonth)
            val targetThisQuarter = dailyTarget * (daysInQuarter - skippedDaysThisQuarter)
            val targetThisYear = dailyTarget * (daysInYear - skippedDaysThisYear)

            val values = ArrayList<Double>()
            if (habit.frequency.denominator <= 1) values.add(valueToday / 1e3)
            if (habit.frequency.denominator <= 7) values.add(valueThisWeek / 1e3)
            values.add(valueThisMonth / 1e3)
            values.add(valueThisQuarter / 1e3)
            values.add(valueThisYear / 1e3)

            val targets = ArrayList<Double>()
            if (habit.frequency.denominator <= 1) targets.add(targetToday)
            if (habit.frequency.denominator <= 7) targets.add(targetThisWeek)
            targets.add(targetThisMonth)
            targets.add(targetThisQuarter)
            targets.add(targetThisYear)

            val intervals = ArrayList<Int>()
            if (habit.frequency.denominator <= 1) intervals.add(1)
            if (habit.frequency.denominator <= 7) intervals.add(7)
            intervals.add(30)
            intervals.add(91)
            intervals.add(365)

            return TargetCardState(
                color = habit.color,
                values = values,
                targets = targets,
                intervals = intervals,
                theme = theme,
            )
        }
    }
}
