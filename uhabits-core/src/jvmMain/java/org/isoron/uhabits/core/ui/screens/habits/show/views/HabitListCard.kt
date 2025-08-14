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

import org.isoron.platform.gui.Color
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import java.util.ArrayList
import java.util.GregorianCalendar

data class HabitListState(
    val habits: List<IndividualHabitListState> = listOf(),
    val weekDayStrings: List<String>,
    val dateStrings: List<String>
)

data class IndividualHabitListState(
    val color: Color,
    val score: Float,
    val isNumerical: Boolean,
    val values: List<Int> = listOf(),
    val targetValue: Double,
    val targetType: NumericalHabitType,
    val name: String,
    val unit: String,
)

class HabitListCardPresenter {
    companion object {
        fun buildState(
            habits: List<Habit>,
            theme: Theme,
            maxDays: Int = 5
        ): HabitListState {
            val habitStateLists = ArrayList<IndividualHabitListState>()
            val weekDayStrings = ArrayList<String>()
            val dateStrings = ArrayList<String>()

            for (habit in habits){
                habitStateLists.add(IndividualHabitListCardPresenter.buildState(habit, theme, maxDays))
            }

            // Get Days
            val day = DateUtils.getStartOfTodayCalendarWithOffset()
            day.add(GregorianCalendar.DAY_OF_MONTH, 0)
            repeat(maxDays) { index ->

                val lines = DateUtils.formatHeaderDate(day).uppercase().split("\n")
                weekDayStrings.add(lines[0])
                dateStrings.add(lines[1])
                day.add(GregorianCalendar.DAY_OF_MONTH, -1)
            }

            return HabitListState(
                habitStateLists,
                weekDayStrings = weekDayStrings,
                dateStrings = dateStrings
            )
        }
    }
}

class IndividualHabitListCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            theme: Theme,
            maxDays: Int
        ): IndividualHabitListState {

            val state = OverviewCardPresenter.buildState(
                habit = habit,
                theme = theme
            )
            val color = theme.color(habit.color)
            val score = state.scoreToday
            val isNumerical = habit.isNumerical
            val today = DateUtils.getTodayWithOffset()
            val values = ArrayList<Int>()
            for (index in 0..maxDays) {
                values.add(habit.computedEntries.get(today.minus(index)).value)
            }
            val targetValue =habit.targetValue
            val targetType = habit.targetType
            val name = habit.name
            val unit = habit.unit

            return IndividualHabitListState(
                color = color,
                score = score,
                isNumerical = isNumerical,
                values = values,
                targetValue = targetValue,
                targetType = targetType,
                name = name,
                unit = unit,
            )
        }
    }
}
