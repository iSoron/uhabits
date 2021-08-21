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
import org.isoron.uhabits.core.ui.views.Theme
import java.util.HashMap

data class FrequencyCardState(
    val color: PaletteColor,
    val firstWeekday: Int,
    val frequency: HashMap<Timestamp, Array<Int>>,
    val theme: Theme,
)

class FrequencyCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            theme: Theme
        ) = FrequencyCardState(
            color = habit.color,
            frequency = habit.originalEntries.computeWeekdayFrequency(
                isNumerical = habit.isNumerical
            ),
            firstWeekday = firstWeekday,
            theme = theme,
        )
    }
}
