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
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.core.ui.callbacks.*
import org.isoron.uhabits.core.utils.*
import java.io.*
import java.util.*

class ShowHabitMenuBehavior(
        private val commandRunner: CommandRunner,
        private val habit: Habit,
        private val habitList: HabitList,
        private val screen: Screen,
        private val system: System,
        private val taskRunner: TaskRunner,
) {
    fun onEditHabit() {
        screen.showEditHabitScreen(habit)
    }

    fun onExportCSV() {
        val outputDir = system.getCSVOutputDir()
        taskRunner.execute(ExportCSVTask(habitList, listOf(habit), outputDir) { filename: String? ->
            if (filename != null) {
                screen.showSendFileScreen(filename)
            } else {
                screen.showMessage(Message.COULD_NOT_EXPORT)
            }
        })
    }

    fun onDeleteHabit() {
        screen.showDeleteConfirmationScreen {
            commandRunner.execute(DeleteHabitsCommand(habitList, listOf(habit)), null)
            screen.close()
        }
    }

    fun onRandomize() {
        val random = Random()
        habit.originalCheckmarks.removeAll()
        var strength = 50.0
        for (i in 0 until 365 * 5) {
            if (i % 7 == 0) strength = Math.max(0.0, Math.min(100.0, strength + 10 * random.nextGaussian()))
            if (random.nextInt(100) > strength) continue
            var value = Checkmark.YES_MANUAL
            if (habit.isNumerical) value = (1000 + 250 * random.nextGaussian() * strength / 100).toInt() * 1000
            habit.originalCheckmarks.setValue(DateUtils.getToday().minus(i), value)
        }
        habit.invalidateNewerThan(Timestamp.ZERO)
        screen.refresh()
    }

    enum class Message {
        COULD_NOT_EXPORT
    }

    interface Screen {
        fun showEditHabitScreen(habit: Habit)
        fun showMessage(m: Message?)
        fun showSendFileScreen(filename: String)
        fun showDeleteConfirmationScreen(
                callback: OnConfirmedCallback)
        fun close()
        fun refresh()
    }

    interface System {
        fun getCSVOutputDir(): File
    }
}