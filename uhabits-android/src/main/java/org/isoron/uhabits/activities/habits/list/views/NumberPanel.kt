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

package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext

data class NumberPanelState(
    val values: List<Double>,
    val completed: List<Boolean>,
    val color: Int,
    val units: String,
    val notes: List<String>,
    val buttonCount: Int
)

class NumberPanelView(
    @ActivityContext context: Context,
    preferences: Preferences,
    private val buttonFactory: NumberButtonViewFactory
) : ButtonPanelView<NumberButtonView>(context, preferences) {

    private var state = NumberPanelState(emptyList(), emptyList(), 0, "", emptyList(), 0)

    private var dataOffset = 0

    var onEdit: (LocalDate) -> Unit = { _ -> }
        set(value) {
            field = value
            setupButtons()
        }

    override fun createButton() = buttonFactory.create()

    fun setState(newState: NumberPanelState) {
        if (newState.buttonCount != buttonCount) {
            buttonCount = newState.buttonCount
            inflateButtons()
        }
        state = newState
        setupButtons()
    }

    fun setDataOffset(offset: Int) {
        dataOffset = offset
        setupButtons()
    }

    @Synchronized
    override fun setupButtons() {
        val today = getToday()

        buttons.forEachIndexed { index, button ->
            val date = today.minus(index + dataOffset)
            button.setState(
                NumberButtonState(
                    state.color,
                    when {
                        index + dataOffset < state.values.size -> state.values[index + dataOffset]
                        else -> 0.0
                    },
                    when {
                        index + dataOffset < state.completed.size -> state.completed[index + dataOffset]
                        else -> false
                    },
                    state.units,
                    when {
                        index + dataOffset < state.notes.size -> state.notes[index + dataOffset]
                        else -> ""
                    }
                )
            )
            button.onEdit = { onEdit(date) }
        }
    }
}

@Inject
class NumberPanelViewFactory(
    @ActivityContext val context: Context,
    val preferences: Preferences,
    val buttonFactory: NumberButtonViewFactory
) {
    fun create() = NumberPanelView(context, preferences, buttonFactory)
}
