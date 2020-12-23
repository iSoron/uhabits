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

import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.callbacks.*
import org.isoron.uhabits.core.ui.screens.habits.list.*

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

    override fun onToggleCheckmark(timestamp: Timestamp, value: Int) {
        if (habit.isNumerical) {
            val checkmarks = habit.checkmarks
            val oldValue = checkmarks.getValues(timestamp, timestamp)[0].toDouble()
            screen.showNumberPicker(oldValue / 1000, habit.unit) { newValue: Double ->
                val thousands = Math.round(newValue * 1000).toInt()
                commandRunner.execute(
                        CreateRepetitionCommand(
                                habitList,
                                habit,
                                timestamp,
                                thousands,
                        ),
                        habit.getId(),
                )
            }
        } else {
            commandRunner.execute(
                    CreateRepetitionCommand(
                            habitList,
                            habit,
                            timestamp,
                            value,
                    ),
                    null,
            )
        }
    }

    interface Screen {
        fun showNumberPicker(value: Double,
                             unit: String,
                             callback: ListHabitsBehavior.NumberPickerCallback)

        fun updateWidgets()
        fun refresh()
        fun showHistoryEditorDialog(listener: OnToggleCheckmarkListener)
    }
}