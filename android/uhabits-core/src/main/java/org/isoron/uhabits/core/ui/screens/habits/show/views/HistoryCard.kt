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

import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import kotlin.math.max

data class HistoryCardViewModel(
    val color: PaletteColor,
    val firstWeekday: Int,
    val series: List<HistoryChart.Square>,
    val theme: Theme,
    val today: LocalDate,
)

class HistoryCardPresenter {
    fun present(
        habit: Habit,
        firstWeekday: Int,
        isSkipEnabled: Boolean,
        theme: Theme,
    ): HistoryCardViewModel {
        val today = DateUtils.getTodayWithOffset()
        val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
        val entries = habit.computedEntries.getByInterval(oldest, today)
        val series = if (habit.isNumerical) {
            entries.map {
                Entry(it.timestamp, max(0, it.value))
            }.map {
                when (it.value) {
                    0 -> HistoryChart.Square.OFF
                    else -> HistoryChart.Square.ON
                }
            }
        } else {
            entries.map {
                when (it.value) {
                    YES_MANUAL -> HistoryChart.Square.ON
                    YES_AUTO -> HistoryChart.Square.DIMMED
                    SKIP -> HistoryChart.Square.HATCHED
                    else -> HistoryChart.Square.OFF
                }
            }
        }

        return HistoryCardViewModel(
            color = habit.color,
            firstWeekday = firstWeekday,
            today = today.toLocalDate(),
            theme = theme,
            series = series,
        )
    }
}
