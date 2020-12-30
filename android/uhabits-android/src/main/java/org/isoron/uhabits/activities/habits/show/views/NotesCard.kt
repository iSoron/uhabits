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

package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.databinding.ShowHabitNotesBinding

data class NotesCardViewModel(val description: String)

class NotesCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val binding = ShowHabitNotesBinding.inflate(LayoutInflater.from(context), this)
    fun update(data: NotesCardViewModel) {
        if (data.description.isEmpty()) {
            visibility = GONE
        } else {
            visibility = VISIBLE
            binding.habitNotes.text = data.description
        }
        invalidate()
    }
}

class NotesCardPresenter(val habit: Habit) {
    fun present() = NotesCardViewModel(
        description = habit.description,
    )
}
