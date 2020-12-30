/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.callbacks.OnToggleCheckmarkListener
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import kotlin.math.roundToInt

class ShowHabitBehavior(
    private val habitList: HabitList,
    private val commandRunner: CommandRunner,
    private val habit: Habit,
    private val screen: Screen,
    private val preferences: Preferences,
) : OnToggleCheckmarkListener {

    fun onScoreCardSpinnerPosition(position: Int) {
        preferences.scoreCardSpinnerPosition = position
        screen.updateWidgets()
        screen.refresh()
    }

    fun onBarCardBoolSpinnerPosition(position: Int) {
        preferences.barCardBoolSpinnerPosition = position
        screen.updateWidgets()
        screen.refresh()
    }

    fun onBarCardNumericalSpinnerPosition(position: Int) {
        preferences.barCardNumericalSpinnerPosition = position
        screen.refresh()
        screen.updateWidgets()
    }

    fun onClickEditHistory() {
        screen.showHistoryEditorDialog(this)
    }

    override fun onToggleEntry(timestamp: Timestamp, value: Int) {
        if (habit.isNumerical) {
            val entries = habit.computedEntries
            val oldValue = entries.get(timestamp).value
            screen.showNumberPicker(oldValue / 1000.0, habit.unit) { newValue: Double ->
                val thousands = (newValue * 1000).roundToInt()
                commandRunner.run(
                    CreateRepetitionCommand(
                        habitList,
                        habit,
                        timestamp,
                        thousands,
                    ),
                )
            }
        } else {
            commandRunner.run(
                CreateRepetitionCommand(
                    habitList,
                    habit,
                    timestamp,
                    value,
                ),
            )
        }
    }

    interface Screen {
        fun showNumberPicker(
            value: Double,
            unit: String,
            callback: ListHabitsBehavior.NumberPickerCallback
        )

        fun updateWidgets()
        fun refresh()
        fun showHistoryEditorDialog(listener: OnToggleCheckmarkListener)
    }
}
