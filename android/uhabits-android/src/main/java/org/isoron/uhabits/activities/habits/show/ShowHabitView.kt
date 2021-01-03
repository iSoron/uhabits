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
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitViewModel
import org.isoron.uhabits.databinding.ShowHabitBinding
import org.isoron.uhabits.utils.setupToolbar

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
