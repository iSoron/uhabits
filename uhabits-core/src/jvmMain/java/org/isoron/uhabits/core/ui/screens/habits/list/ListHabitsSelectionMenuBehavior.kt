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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.isoron.uhabits.core.commands.ArchiveHabitsCommand
import org.isoron.uhabits.core.commands.ChangeHabitColorCommand
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.commands.UnarchiveHabitsCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import javax.inject.Inject

class ListHabitsSelectionMenuBehavior @Inject constructor(
    private val habitList: HabitList,
    private val screen: Screen,
    private val adapter: Adapter,
    var commandRunner: CommandRunner
) {
    fun canArchive(): Boolean {
        for (habit in adapter.getSelected()) if (habit.isArchived) return false
        return true
    }

    fun canEdit(): Boolean {
        return adapter.getSelected().size == 1
    }

    fun canUnarchive(): Boolean {
        for (habit in adapter.getSelected()) if (!habit.isArchived) return false
        return true
    }

    fun onArchiveHabits() {
        commandRunner.run(ArchiveHabitsCommand(habitList, adapter.getSelected()))
        adapter.clearSelection()
    }

    fun onChangeColor() {
        val (color) = adapter.getSelected()[0]
        screen.showColorPicker(color) { selectedColor: PaletteColor ->
            commandRunner.run(ChangeHabitColorCommand(habitList, adapter.getSelected(), selectedColor))
            adapter.clearSelection()
        }
    }

    fun onDeleteHabits() {
        screen.showDeleteConfirmationScreen(
            {
                adapter.performRemove(adapter.getSelected())
                commandRunner.run(DeleteHabitsCommand(habitList, adapter.getSelected()))
                adapter.clearSelection()
            },
            adapter.getSelected().size
        )
    }

    fun onEditHabits() {
        val selected = adapter.getSelected()
        if (selected.isNotEmpty()) screen.showEditHabitsScreen(selected)
        adapter.clearSelection()
    }

    fun onUnarchiveHabits() {
        commandRunner.run(UnarchiveHabitsCommand(habitList, adapter.getSelected()))
        adapter.clearSelection()
    }

    interface Adapter {
        fun clearSelection()
        fun getSelected(): List<Habit>
        fun performRemove(selected: List<Habit>)
    }

    interface Screen {
        fun showColorPicker(
            defaultColor: PaletteColor,
            callback: OnColorPickedCallback
        )

        fun showDeleteConfirmationScreen(
            callback: OnConfirmedCallback,
            quantity: Int
        )

        fun showEditHabitsScreen(selected: List<Habit>)
    }
}
