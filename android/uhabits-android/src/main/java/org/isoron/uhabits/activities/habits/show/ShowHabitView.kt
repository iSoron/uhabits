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

package org.isoron.uhabits.activities.habits.show

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.isoron.uhabits.activities.habits.show.views.BarCardPresenter
import org.isoron.uhabits.activities.habits.show.views.BarCardViewModel
import org.isoron.uhabits.activities.habits.show.views.FrequencyCardPresenter
import org.isoron.uhabits.activities.habits.show.views.FrequencyCardViewModel
import org.isoron.uhabits.activities.habits.show.views.HistoryCardPresenter
import org.isoron.uhabits.activities.habits.show.views.HistoryCardViewModel
import org.isoron.uhabits.activities.habits.show.views.NotesCardPresenter
import org.isoron.uhabits.activities.habits.show.views.NotesCardViewModel
import org.isoron.uhabits.activities.habits.show.views.OverviewCardPresenter
import org.isoron.uhabits.activities.habits.show.views.OverviewCardViewModel
import org.isoron.uhabits.activities.habits.show.views.ScoreCardPresenter
import org.isoron.uhabits.activities.habits.show.views.ScoreCardViewModel
import org.isoron.uhabits.activities.habits.show.views.StreakCardViewModel
import org.isoron.uhabits.activities.habits.show.views.StreakCartPresenter
import org.isoron.uhabits.activities.habits.show.views.SubtitleCardPresenter
import org.isoron.uhabits.activities.habits.show.views.SubtitleCardViewModel
import org.isoron.uhabits.activities.habits.show.views.TargetCardPresenter
import org.isoron.uhabits.activities.habits.show.views.TargetCardViewModel
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.databinding.ShowHabitBinding
import org.isoron.uhabits.utils.setupToolbar

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

class ShowHabitView(context: Context) : FrameLayout(context) {
    private val binding = ShowHabitBinding.inflate(LayoutInflater.from(context))

    var onScoreCardSpinnerPosition: (position: Int) -> Unit = {}
    var onClickEditHistoryButton: () -> Unit = {}
    var onBarCardBoolSpinnerPosition: (position: Int) -> Unit = {}
    var onBarCardNumericalSpinnerPosition: (position: Int) -> Unit = {}

    init {
        addView(binding.root)
        binding.scoreCard.onSpinnerPosition = { onScoreCardSpinnerPosition(it) }
        binding.historyCard.onClickEditButton = { onClickEditHistoryButton() }
        binding.barCard.onBoolSpinnerPosition = { onBarCardBoolSpinnerPosition(it) }
        binding.barCard.onNumericalSpinnerPosition = { onBarCardNumericalSpinnerPosition(it) }
    }

    fun update(data: ShowHabitViewModel) {
        setupToolbar(binding.toolbar, title = data.title, color = data.color)
        binding.subtitleCard.update(data.subtitle)
        binding.overviewCard.update(data.overview)
        binding.notesCard.update(data.notes)
        binding.targetCard.update(data.target)
        binding.streakCard.update(data.streaks)
        binding.scoreCard.update(data.scores)
        binding.frequencyCard.update(data.frequency)
        binding.historyCard.update(data.history)
        binding.barCard.update(data.bar)
        if (data.isNumerical) {
            binding.overviewCard.visibility = GONE
            binding.streakCard.visibility = GONE
        } else {
            binding.targetCard.visibility = GONE
        }
    }
}

class ShowHabitPresenter(
    val habit: Habit,
    val context: Context,
    val preferences: Preferences,
) {
    private val subtitleCardPresenter = SubtitleCardPresenter(habit, context)
    private val overviewCardPresenter = OverviewCardPresenter(habit)
    private val notesCardPresenter = NotesCardPresenter(habit)
    private val targetCardPresenter = TargetCardPresenter(
        habit = habit,
        firstWeekday = preferences.firstWeekday,
        resources = context.resources,
    )
    private val streakCartPresenter = StreakCartPresenter(habit)
    private val scoreCardPresenter = ScoreCardPresenter(
        habit = habit,
        firstWeekday = preferences.firstWeekday,
    )
    private val frequencyCardPresenter = FrequencyCardPresenter(
        habit = habit,
        firstWeekday = preferences.firstWeekday,
    )
    private val historyCardViewModel = HistoryCardPresenter(
        habit = habit,
        firstWeekday = preferences.firstWeekday,
        isSkipEnabled = preferences.isSkipEnabled,
    )
    private val barCardPresenter = BarCardPresenter(
        habit = habit,
        firstWeekday = preferences.firstWeekday,
    )

    suspend fun present(): ShowHabitViewModel {
        return ShowHabitViewModel(
            title = habit.name,
            color = habit.color,
            isNumerical = habit.isNumerical,
            subtitle = subtitleCardPresenter.present(),
            overview = overviewCardPresenter.present(),
            notes = notesCardPresenter.present(),
            target = targetCardPresenter.present(),
            streaks = streakCartPresenter.present(),
            scores = scoreCardPresenter.present(
                spinnerPosition = preferences.scoreCardSpinnerPosition
            ),
            frequency = frequencyCardPresenter.present(),
            history = historyCardViewModel.present(),
            bar = barCardPresenter.present(
                boolSpinnerPosition = preferences.barCardBoolSpinnerPosition,
                numericalSpinnerPosition = preferences.barCardNumericalSpinnerPosition,
            ),
        )
    }
}
