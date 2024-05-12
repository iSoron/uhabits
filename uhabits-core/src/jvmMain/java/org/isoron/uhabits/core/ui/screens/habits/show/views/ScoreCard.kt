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

package org.isoron.uhabits.core.ui.screens.habits.show.views

import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.TruncateField
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.views.Theme

data class ScoreCardState(
    val scores: List<Score>,
    val bucketSize: Int,
    val spinnerPosition: Int,
    val color: PaletteColor,
    val theme: Theme
)

class ScoreCardPresenter(
    val preferences: Preferences,
    val screen: Screen
) {
    companion object {
        val BUCKET_SIZES = intArrayOf(1, 7, 31, 92, 365)
        fun getTruncateField(bucketSize: Int): TruncateField {
            return when (bucketSize) {
                1 -> TruncateField.DAY
                7 -> TruncateField.WEEK_NUMBER
                31 -> TruncateField.MONTH
                92 -> TruncateField.QUARTER
                365 -> TruncateField.YEAR
                else -> TruncateField.MONTH
            }
        }

        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            spinnerPosition: Int,
            theme: Theme
        ): ScoreCardState {
            val bucketSize = BUCKET_SIZES[spinnerPosition]
            val today = getToday()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.date ?: today

            val scores = habit.scores.getByInterval(oldest, today).groupBy { score ->
                truncateDate(getTruncateField(bucketSize), score.date, firstWeekday)
            }.map { (date, scores) ->
                Score(
                    date,
                    scores.map {
                        it.value
                    }.average()
                )
            }.sortedBy {
                it.date
            }.reversed()

            return ScoreCardState(
                color = habit.color,
                scores = scores,
                bucketSize = bucketSize,
                spinnerPosition = spinnerPosition,
                theme = theme
            )
        }

        private fun truncateDate(
            field: TruncateField,
            date: LocalDate,
            firstWeekday: Int
        ): LocalDate {
            // firstWeekday: 1=Sunday, 2=Monday, ..., 7=Saturday
            // DayOfWeek enum: SUNDAY(0), MONDAY(1), ..., SATURDAY(6)
            val firstWeekdayDow = org.isoron.platform.time.DayOfWeek.entries[firstWeekday - 1]
            return when (field) {
                TruncateField.WEEK_NUMBER -> date.startOfWeek(firstWeekdayDow)
                TruncateField.MONTH -> date.startOfMonth()
                TruncateField.QUARTER -> date.startOfQuarter()
                TruncateField.YEAR -> date.startOfYear()
                else -> date
            }
        }

        fun buildState(
            habitGroup: HabitGroup,
            firstWeekday: Int,
            spinnerPosition: Int,
            theme: Theme
        ): ScoreCardState {
            val bucketSize = BUCKET_SIZES[spinnerPosition]
            val today = getToday()
            val oldest = if (habitGroup.habitList.isEmpty) {
                today
            } else {
                habitGroup.habitList.minOf {
                    it.computedEntries.getKnown().lastOrNull()?.date ?: today
                }
            }

            val field = getTruncateField(bucketSize)
            val scores = habitGroup.scores.getByInterval(oldest, today).groupBy {
                DateUtils.truncate(field, it.date, firstWeekday)
            }.map { (date, scores) ->
                Score(
                    date,
                    scores.map {
                        it.value
                    }.average()
                )
            }.sortedBy {
                it.date
            }.reversed()

            return ScoreCardState(
                color = habitGroup.color,
                scores = scores,
                bucketSize = bucketSize,
                spinnerPosition = spinnerPosition,
                theme = theme
            )
        }
    }

    fun onSpinnerPosition(position: Int) {
        preferences.scoreCardSpinnerPosition = position
        screen.updateWidgets()
        screen.refresh()
    }

    interface Screen {
        fun updateWidgets()
        fun refresh()
    }
}
