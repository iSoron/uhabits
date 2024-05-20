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
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.countSkippedDays
import org.isoron.uhabits.core.models.groupedSum
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import java.util.ArrayList
import java.util.Calendar
import kotlin.math.max

data class TargetCardState(
    val color: PaletteColor,
    val values: List<Double> = listOf(),
    val targets: List<Double> = listOf(),
    val intervals: List<Int> = listOf(),
    val theme: Theme
)

class TargetCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            theme: Theme
        ): TargetCardState {
            val today = DateUtils.getTodayWithOffset()
            val (yearBegin, yearEnd) = getYearRange(firstWeekday)
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
            val entriesForSkip = habit.computedEntries.getByInterval(yearBegin, yearEnd, habit.skipDays)
            val entriesForSum = habit.computedEntries.getByInterval(oldest, today)

            val valueToday = entriesForSum.groupedSum(
                truncateField = DateUtils.TruncateField.DAY,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDayToday = entriesForSkip.countSkippedDays(
                truncateField = DateUtils.TruncateField.DAY,
                skipDays = habit.skipDays,
            ).firstOrNull()?.value ?: 0

            val valueThisWeek = entriesForSum.groupedSum(
                truncateField = DateUtils.TruncateField.WEEK_NUMBER,
                firstWeekday = firstWeekday,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisWeek = entriesForSkip.countSkippedDays(
                truncateField = DateUtils.TruncateField.WEEK_NUMBER,
                firstWeekday = firstWeekday,
                skipDays = habit.skipDays,
            ).firstOrNull()?.value ?: 0

            val valueThisMonth = entriesForSum.groupedSum(
                truncateField = DateUtils.TruncateField.MONTH,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisMonth = entriesForSkip.countSkippedDays(
                truncateField = DateUtils.TruncateField.MONTH,
                skipDays = habit.skipDays,
            ).firstOrNull()?.value ?: 0

            val valueThisQuarter = entriesForSum.groupedSum(
                truncateField = DateUtils.TruncateField.QUARTER,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisQuarter = entriesForSkip.countSkippedDays(
                truncateField = DateUtils.TruncateField.QUARTER,
                skipDays = habit.skipDays,
            ).firstOrNull()?.value ?: 0

            val valueThisYear = entriesForSum.groupedSum(
                truncateField = DateUtils.TruncateField.YEAR,
                isNumerical = habit.isNumerical
            ).firstOrNull()?.value ?: 0

            val skippedDaysThisYear = entriesForSkip.countSkippedDays(
                truncateField = DateUtils.TruncateField.YEAR,
                skipDays = habit.skipDays,
            ).firstOrNull()?.value ?: 0

            val cal = DateUtils.getStartOfTodayCalendarWithOffset()
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysInWeek = 7
            val daysInQuarter = 91
            val daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
            val weeksInMonth = daysInMonth / 7
            val weeksInQuarter = 13
            val weeksInYear = 52
            val monthsInQuarter = 3
            val monthsInYear = 12

            val denominator = habit.frequency.denominator
            val dailyTarget = habit.targetValue / habit.frequency.denominator

            var targetToday = dailyTarget
            var targetThisWeek = when (denominator) {
                7 -> habit.targetValue
                else -> dailyTarget * daysInWeek
            }
            var targetThisMonth = when (denominator) {
                30 -> habit.targetValue
                7 -> habit.targetValue * weeksInMonth
                else -> dailyTarget * daysInMonth
            }
            var targetThisQuarter = when (denominator) {
                30 -> habit.targetValue * monthsInQuarter
                7 -> habit.targetValue * weeksInQuarter
                else -> dailyTarget * daysInQuarter
            }
            var targetThisYear = when (denominator) {
                30 -> habit.targetValue * monthsInYear
                7 -> habit.targetValue * weeksInYear
                else -> dailyTarget * daysInYear
            }

            targetToday = max(0.0, targetToday - dailyTarget * skippedDayToday)
            targetThisWeek = max(0.0, targetThisWeek - dailyTarget * skippedDaysThisWeek)
            targetThisMonth = max(0.0, targetThisMonth - dailyTarget * skippedDaysThisMonth)
            targetThisQuarter = max(0.0, targetThisQuarter - dailyTarget * skippedDaysThisQuarter)
            targetThisYear = max(0.0, targetThisYear - dailyTarget * skippedDaysThisYear)

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
                theme = theme
            )
        }

        private fun getYearRange(firstWeekday: Int): Pair<Timestamp, Timestamp> {
            val today = DateUtils.getTodayWithOffset()
            val yearBegin = today.truncate(DateUtils.TruncateField.YEAR, firstWeekday)
            val cali = yearBegin.toCalendar()
            cali.add(Calendar.YEAR, 1)
            var newest = Timestamp(cali)
            val thisWeek = today.truncate(DateUtils.TruncateField.WEEK_NUMBER, firstWeekday)
            if (thisWeek.daysUntil(newest) < 7) newest = thisWeek.plus(7)
            return Pair(yearBegin, newest)
        }
    }
}
