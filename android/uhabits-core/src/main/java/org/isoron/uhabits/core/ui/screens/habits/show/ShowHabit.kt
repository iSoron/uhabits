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

package org.isoron.uhabits.core.ui.screens.habits.show

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.show.views.BarCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.BarCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.FrequencyCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.FrequencyCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCartPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardViewModel
import org.isoron.uhabits.core.ui.screens.habits.show.views.TargetCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.TargetCardViewModel
import org.isoron.uhabits.core.ui.views.Theme

data class ShowHabitViewModel(
    val title: String = "",
    val isNumerical: Boolean = false,
    val color: PaletteColor = PaletteColor(1),
    val subtitle: SubtitleCardViewModel,
    val overview: OverviewCardViewModel,
    val notes: NotesCardViewModel,
    val target: TargetCardViewModel,
    val streaks: StreakCardViewModel,
    val scores: ScoreCardViewModel,
    val frequency: FrequencyCardViewModel,
    val history: HistoryCardViewModel,
    val bar: BarCardViewModel,
)

class ShowHabitPresenter {
    fun present(
        habit: Habit,
        preferences: Preferences,
        theme: Theme,
    ): ShowHabitViewModel {
        return ShowHabitViewModel(
            title = habit.name,
            color = habit.color,
            isNumerical = habit.isNumerical,
            subtitle = SubtitleCardPresenter().present(
                habit = habit,
            ),
            overview = OverviewCardPresenter().present(
                habit = habit,
            ),
            notes = NotesCardPresenter().present(
                habit = habit,
            ),
            target = TargetCardPresenter().present(
                habit = habit,
                firstWeekday = preferences.firstWeekday,
            ),
            streaks = StreakCartPresenter().present(
                habit = habit,
            ),
            scores = ScoreCardPresenter().present(
                spinnerPosition = preferences.scoreCardSpinnerPosition,
                habit = habit,
                firstWeekday = preferences.firstWeekday,
            ),
            frequency = FrequencyCardPresenter().present(
                habit = habit,
                firstWeekday = preferences.firstWeekday,
            ),
            history = HistoryCardPresenter().present(
                habit = habit,
                firstWeekday = preferences.firstWeekday,
                isSkipEnabled = preferences.isSkipEnabled,
                theme = theme,
            ),
            bar = BarCardPresenter().present(
                habit = habit,
                firstWeekday = preferences.firstWeekday,
                boolSpinnerPosition = preferences.barCardBoolSpinnerPosition,
                numericalSpinnerPosition = preferences.barCardNumericalSpinnerPosition,
                theme = theme,
            ),
        )
    }
}
