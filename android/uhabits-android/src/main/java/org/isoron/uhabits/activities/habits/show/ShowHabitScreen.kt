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

import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.ui.callbacks.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.core.ui.screens.habits.show.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.*


class ShowHabitScreen(
        val activity: ShowHabitActivity,
        val confirmDeleteDialogFactory: ConfirmDeleteDialogFactory,
        val habit: Habit,
        val intentFactory: IntentFactory,
        val numberPickerFactory: NumberPickerFactory,
        val widgetUpdater: WidgetUpdater,
) : ShowHabitBehavior.Screen, ShowHabitMenuBehavior.Screen {

    override fun showNumberPicker(value: Double, unit: String, callback: ListHabitsBehavior.NumberPickerCallback) {
        numberPickerFactory.create(value, unit, callback).show();
    }

    override fun updateWidgets() {
        widgetUpdater.updateWidgets(habit.id)
    }

    override fun refresh() {
        activity.refresh()
    }

    override fun showHistoryEditorDialog(listener: OnToggleCheckmarkListener) {
        val dialog = HistoryEditorDialog()
        dialog.setHabit(habit)
        dialog.setOnToggleCheckmarkListener(listener)
        dialog.show(activity.supportFragmentManager, "historyEditor")
    }

    override fun showEditHabitScreen(habit: Habit) {
        activity.startActivity(intentFactory.startEditActivity(activity, habit))
    }

    override fun showMessage(m: ShowHabitMenuBehavior.Message?) {
        when (m) {
            ShowHabitMenuBehavior.Message.COULD_NOT_EXPORT -> {
                activity.showMessage(org.isoron.uhabits.R.string.could_not_export)
            }
        }
    }

    override fun showSendFileScreen(filename: String) {
        activity.showSendFileScreen(filename)
    }

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
        confirmDeleteDialogFactory.create(callback).show()
    }

    override fun close() {
        activity.finish()
    }
}