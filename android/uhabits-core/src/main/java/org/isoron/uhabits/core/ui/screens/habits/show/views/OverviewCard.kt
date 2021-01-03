/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.utils.DateUtils

data class OverviewCardViewModel(
    val color: PaletteColor,
    val scoreMonthDiff: Float,
    val scoreYearDiff: Float,
    val scoreToday: Float,
    val totalCount: Long,
)

class OverviewCardPresenter {
    fun present(habit: Habit): OverviewCardViewModel {
        val today = DateUtils.getTodayWithOffset()
        val lastMonth = today.minus(30)
        val lastYear = today.minus(365)
        val scores = habit.scores
        val scoreToday = scores.get(today).value.toFloat()
        val scoreLastMonth = scores.get(lastMonth).value.toFloat()
        val scoreLastYear = scores.get(lastYear).value.toFloat()
        val totalCount = habit.originalEntries.getKnown()
            .filter { it.value == Entry.YES_MANUAL }
            .count()
            .toLong()
        return OverviewCardViewModel(
            color = habit.color,
            scoreToday = scoreToday,
            scoreMonthDiff = scoreToday - scoreLastMonth,
            scoreYearDiff = scoreToday - scoreLastYear,
            totalCount = totalCount,
        )
    }
}
