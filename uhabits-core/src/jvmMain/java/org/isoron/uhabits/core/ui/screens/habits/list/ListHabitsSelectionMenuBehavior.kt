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

import org.isoron.uhabits.core.commands.ArchiveHabitGroupsCommand
import org.isoron.uhabits.core.commands.ArchiveHabitsCommand
import org.isoron.uhabits.core.commands.ChangeHabitColorCommand
import org.isoron.uhabits.core.commands.ChangeHabitGroupColorCommand
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.DeleteHabitGroupsCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.commands.RefreshParentGroupCommand
import org.isoron.uhabits.core.commands.RemoveFromGroupCommand
import org.isoron.uhabits.core.commands.UnarchiveHabitGroupsCommand
import org.isoron.uhabits.core.commands.UnarchiveHabitsCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import javax.inject.Inject

class ListHabitsSelectionMenuBehavior @Inject constructor(
    private val habitList: HabitList,
    private val habitGroupList: HabitGroupList,
    private val screen: Screen,
    private val adapter: Adapter,
    var commandRunner: CommandRunner
) {
    fun canArchive(): Boolean {
        for (habit in adapter.getSelectedHabits()) if (habit.isArchived) return false
        for (hgr in adapter.getSelectedHabitGroups()) if (hgr.isArchived) return false
        return true
    }

    fun canEdit(): Boolean {
        return (adapter.getSelectedHabits().size + adapter.getSelectedHabitGroups().size == 1)
    }

    fun canUnarchive(): Boolean {
        for (habit in adapter.getSelectedHabits()) if (!habit.isArchived) return false
        for (hgr in adapter.getSelectedHabitGroups()) if (!hgr.isArchived) return false
        return true
    }

    fun areSubHabits(): Boolean {
        if (adapter.getSelectedHabitGroups().isNotEmpty()) return false
        return (adapter.getSelectedHabits().all { it.isSubHabit() })
    }

    fun areHabits(): Boolean {
        return adapter.getSelectedHabitGroups().isEmpty()
    }

    fun onArchiveHabits() {
        commandRunner.run(ArchiveHabitsCommand(habitList, adapter.getSelectedHabits()))
        commandRunner.run(ArchiveHabitGroupsCommand(habitGroupList, adapter.getSelectedHabitGroups()))
        for (habit in adapter.getSelectedHabits()) {
            commandRunner.run(RefreshParentGroupCommand(habit, habitGroupList))
        }
        adapter.clearSelection()
    }

    fun onChangeColor() {
        val color = if (adapter.getSelectedHabits().isNotEmpty()) {
            adapter.getSelectedHabits()[0].color
        } else {
            adapter.getSelectedHabitGroups()[0].color
        }

        screen.showColorPicker(color) { selectedColor: PaletteColor ->
            commandRunner.run(
                ChangeHabitColorCommand(
                    habitList,
                    adapter.getSelectedHabits(),
                    selectedColor
                )
            )
            commandRunner.run(
                ChangeHabitGroupColorCommand(
                    habitGroupList,
                    adapter.getSelectedHabitGroups(),
                    selectedColor
                )
            )
            adapter.clearSelection()
        }
    }

    fun onDeleteHabits() {
        screen.showDeleteConfirmationScreen(
            {
                adapter.performRemove(adapter.getSelectedHabits())
                adapter.performRemoveHabitGroup(adapter.getSelectedHabitGroups())
                commandRunner.run(DeleteHabitGroupsCommand(habitGroupList, adapter.getSelectedHabitGroups()))
                commandRunner.run(DeleteHabitsCommand(habitList, adapter.getSelectedHabits()))
                for (habit in adapter.getSelectedHabits()) {
                    commandRunner.run(RefreshParentGroupCommand(habit, habitGroupList))
                }
                adapter.clearSelection()
            },
            adapter.getSelectedHabits().size + adapter.getSelectedHabitGroups().size
        )
    }

    fun onEditHabits() {
        val selected = adapter.getSelectedHabits()
        if (selected.isNotEmpty()) {
            screen.showEditHabitsScreen(selected)
        } else {
            val selectedGroup = adapter.getSelectedHabitGroups()
            screen.showEditHabitGroupScreen(selectedGroup)
        }

        adapter.clearSelection()
    }

    fun onUnarchiveHabits() {
        commandRunner.run(UnarchiveHabitsCommand(habitList, adapter.getSelectedHabits()))
        commandRunner.run(UnarchiveHabitGroupsCommand(habitGroupList, adapter.getSelectedHabitGroups()))
        for (habit in adapter.getSelectedHabits()) {
            commandRunner.run(RefreshParentGroupCommand(habit, habitGroupList))
        }
        adapter.clearSelection()
    }

    fun onRemoveFromGroup() {
        adapter.performRemove(adapter.getSelectedHabits())
        commandRunner.run(RemoveFromGroupCommand(habitList, adapter.getSelectedHabits()))
        adapter.clearSelection()
    }

    fun onAddToGroup() {
        adapter.performRemove(adapter.getSelectedHabits())
        screen.showHabitGroupPickerDialog(adapter.getSelectedHabits())
        adapter.clearSelection()
    }

    interface Adapter {
        fun clearSelection()
        fun getSelectedHabits(): List<Habit>
        fun getSelectedHabitGroups(): List<HabitGroup>
        fun performRemove(selected: List<Habit>)
        fun performRemoveHabitGroup(selected: List<HabitGroup>)
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

        fun showEditHabitGroupScreen(selected: List<HabitGroup>)
        fun showHabitGroupPickerDialog(selected: List<Habit>)
    }
}
