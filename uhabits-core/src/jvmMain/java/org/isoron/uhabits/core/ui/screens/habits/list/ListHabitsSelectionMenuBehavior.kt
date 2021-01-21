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
        for ((_, _, _, _, isArchived) in adapter.selected) if (isArchived) return false
        return true
    }

    fun canEdit(): Boolean {
        return adapter.selected.size == 1
    }

    fun canUnarchive(): Boolean {
        for ((_, _, _, _, isArchived) in adapter.selected) if (!isArchived) return false
        return true
    }

    fun onArchiveHabits() {
        commandRunner.run(ArchiveHabitsCommand(habitList, adapter.selected))
        adapter.clearSelection()
    }

    fun onChangeColor() {
        val selected = adapter.selected
        val (color) = selected[0]
        screen.showColorPicker(color) { selectedColor: PaletteColor ->
            commandRunner.run(ChangeHabitColorCommand(habitList, selected, selectedColor))
            adapter.clearSelection()
        }
    }

    fun onDeleteHabits() {
        val selected = adapter.selected
        screen.showDeleteConfirmationScreen(
            {
                adapter.performRemove(selected)
                commandRunner.run(DeleteHabitsCommand(habitList, selected))
                adapter.clearSelection()
            },
            selected.size
        )
    }

    fun onEditHabits() {
        screen.showEditHabitsScreen(adapter.selected)
        adapter.clearSelection()
    }

    fun onUnarchiveHabits() {
        commandRunner.run(UnarchiveHabitsCommand(habitList, adapter.selected))
        adapter.clearSelection()
    }

    interface Adapter {
        fun clearSelection()
        val selected: List<Habit>
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
