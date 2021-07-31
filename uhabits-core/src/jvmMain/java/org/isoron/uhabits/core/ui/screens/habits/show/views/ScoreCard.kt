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
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils

data class ScoreCardState(
    val scores: List<Score>,
    val bucketSize: Int,
    val spinnerPosition: Int,
    val color: PaletteColor,
    val theme: Theme,
)

class ScoreCardPresenter(
    val preferences: Preferences,
    val screen: Screen,
) {
    companion object {
        val BUCKET_SIZES = intArrayOf(1, 7, 31, 92, 365)
        fun getTruncateField(bucketSize: Int): DateUtils.TruncateField {
            return when (bucketSize) {
                1 -> DateUtils.TruncateField.DAY
                7 -> DateUtils.TruncateField.WEEK_NUMBER
                31 -> DateUtils.TruncateField.MONTH
                92 -> DateUtils.TruncateField.QUARTER
                365 -> DateUtils.TruncateField.YEAR
                else -> DateUtils.TruncateField.MONTH
            }
        }

        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            spinnerPosition: Int,
            theme: Theme,
        ): ScoreCardState {
            val bucketSize = BUCKET_SIZES[spinnerPosition]
            val today = DateUtils.getTodayWithOffset()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today

            val field = getTruncateField(bucketSize)
            val scores = habit.scores.getByInterval(oldest, today).groupBy {
                DateUtils.truncate(field, it.timestamp, firstWeekday)
            }.map { (timestamp, scores) ->
                Score(
                    timestamp,
                    scores.map {
                        it.value
                    }.average()
                )
            }.sortedBy {
                it.timestamp
            }.reversed()

            return ScoreCardState(
                color = habit.color,
                scores = scores,
                bucketSize = bucketSize,
                spinnerPosition = spinnerPosition,
                theme = theme,
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
