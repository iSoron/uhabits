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

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.utils.DateUtils

data class HistoryCardViewModel(
    val color: PaletteColor,
    val entries: IntArray,
    val firstWeekday: Int,
    val isNumerical: Boolean,
    val isSkipEnabled: Boolean,
)

class HistoryCardPresenter {
    fun present(
        habit: Habit,
        firstWeekday: Int,
        isSkipEnabled: Boolean,
    ): HistoryCardViewModel {
        val today = DateUtils.getTodayWithOffset()
        val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
        val entries =
            habit.computedEntries.getByInterval(oldest, today).map { it.value }.toIntArray()

        return HistoryCardViewModel(
            entries = entries,
            color = habit.color,
            firstWeekday = firstWeekday,
            isNumerical = habit.isNumerical,
            isSkipEnabled = isSkipEnabled,
        )
    }
}
