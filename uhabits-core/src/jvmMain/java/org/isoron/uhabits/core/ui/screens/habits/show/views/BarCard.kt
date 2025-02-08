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

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.groupedSum
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils

data class BarCardState(
    val theme: Theme,
    val boolSpinnerPosition: Int,
    val bucketSize: Int,
    val color: PaletteColor,
    val entries: List<Entry>,
    val isNumerical: Boolean,
    val numericalSpinnerPosition: Int
)

class BarCardPresenter(
    val preferences: Preferences,
    val screen: Screen
) {
    companion object {
        val numericalBucketSizes = intArrayOf(1, 7, 31, 92, 365)
        val boolBucketSizes = intArrayOf(7, 31, 92, 365)

        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            numericalSpinnerPosition: Int,
            boolSpinnerPosition: Int,
            theme: Theme
        ): BarCardState {
            val bucketSize = if (habit.isNumerical) {
                numericalBucketSizes[numericalSpinnerPosition]
            } else {
                boolBucketSizes[boolSpinnerPosition]
            }
            val today = DateUtils.getTodayWithOffset()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
            val entries = habit.computedEntries.getByInterval(oldest, today).groupedSum(
                truncateField = ScoreCardPresenter.getTruncateField(bucketSize),
                firstWeekday = firstWeekday,
                isNumerical = habit.isNumerical
            )
            return BarCardState(
                theme = theme,
                entries = entries,
                bucketSize = bucketSize,
                color = habit.color,
                isNumerical = habit.isNumerical,
                numericalSpinnerPosition = numericalSpinnerPosition,
                boolSpinnerPosition = boolSpinnerPosition
            )
        }

        fun buildState(
            habitGroup: HabitGroup,
            firstWeekday: Int,
            numericalSpinnerPosition: Int,
            boolSpinnerPosition: Int,
            theme: Theme
        ): BarCardState {
            val isNumerical = habitGroup.habitList.all { it.isNumerical }
            val isBoolean = habitGroup.habitList.all { !it.isNumerical }
            if ((!isNumerical && !isBoolean) || habitGroup.habitList.isEmpty) {
                return BarCardState(
                    theme = theme,
                    entries = listOf(Entry(DateUtils.getTodayWithOffset(), 0)),
                    bucketSize = 1,
                    color = habitGroup.color,
                    isNumerical = isNumerical,
                    numericalSpinnerPosition = numericalSpinnerPosition,
                    boolSpinnerPosition = boolSpinnerPosition
                )
            }
            val bucketSize = if (isNumerical) {
                numericalBucketSizes[numericalSpinnerPosition]
            } else {
                boolBucketSizes[boolSpinnerPosition]
            }
            val today = DateUtils.getTodayWithOffset()
            val allEntries = habitGroup.habitList.map { habit ->
                val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
                habit.computedEntries.getByInterval(oldest, today).groupedSum(
                    truncateField = ScoreCardPresenter.getTruncateField(bucketSize),
                    firstWeekday = firstWeekday,
                    isNumerical = habit.isNumerical
                )
            }.flatten()

            val summedEntries = allEntries.groupedSum()
            return BarCardState(
                theme = theme,
                entries = summedEntries,
                bucketSize = bucketSize,
                color = habitGroup.color,
                isNumerical = isNumerical,
                numericalSpinnerPosition = numericalSpinnerPosition,
                boolSpinnerPosition = boolSpinnerPosition
            )
        }
    }

    fun onNumericalSpinnerPosition(position: Int) {
        preferences.barCardNumericalSpinnerPosition = position
        screen.updateWidgets()
        screen.refresh()
    }

    fun onBoolSpinnerPosition(position: Int) {
        preferences.barCardBoolSpinnerPosition = position
        screen.updateWidgets()
        screen.refresh()
    }

    interface Screen {
        fun updateWidgets()
        fun refresh()
    }
}
