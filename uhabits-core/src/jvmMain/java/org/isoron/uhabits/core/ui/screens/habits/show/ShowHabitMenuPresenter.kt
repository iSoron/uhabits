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
package org.isoron.uhabits.core.ui.screens.habits.show

import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.tasks.ExportCSVTask
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.isoron.uhabits.core.utils.DateUtils
import java.io.File
import java.util.Random
import kotlin.math.max
import kotlin.math.min

class ShowHabitMenuPresenter(
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
        taskRunner.execute(
            ExportCSVTask(habitList, listOf(habit), outputDir) { filename: String? ->
                if (filename != null) {
                    screen.showSendFileScreen(filename)
                } else {
                    screen.showMessage(Message.COULD_NOT_EXPORT)
                }
            }
        )
    }

    fun onDeleteHabit() {
        screen.showDeleteConfirmationScreen {
            commandRunner.run(DeleteHabitsCommand(habitList, listOf(habit)))
            screen.close()
        }
    }

    fun onRandomize() {
        val random = Random()
        habit.originalEntries.clear()
        var strength = 50.0
        for (i in 0 until 365 * 5) {
            if (i % 7 == 0) strength = max(0.0, min(100.0, strength + 10 * random.nextGaussian()))
            if (random.nextInt(100) > strength) continue
            var value = Entry.YES_MANUAL
            if (habit.isNumerical) value = (1000 + 250 * random.nextGaussian() * strength / 100).toInt() * 1000
            habit.originalEntries.add(Entry(DateUtils.getToday().minus(i), value))
        }
        habit.recompute()
        screen.refresh()
    }

    enum class Message {
        COULD_NOT_EXPORT
    }

    interface Screen {
        fun showEditHabitScreen(habit: Habit)
        fun showMessage(m: Message?)
        fun showSendFileScreen(filename: String)
        fun showDeleteConfirmationScreen(callback: OnConfirmedCallback)
        fun close()
        fun refresh()
    }

    interface System {
        fun getCSVOutputDir(): File
    }
}
