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
package org.isoron.uhabits.core.ui.screens.habits.list

import me.tatarka.inject.annotations.Inject
import org.isoron.platform.io.UserFile
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.RenameCategoryCommand
import org.isoron.uhabits.core.commands.ReorderHabitCommand
import org.isoron.uhabits.core.commands.ReorderTarget
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.NumericalHabitType.AT_LEAST
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.ExportCSVTask
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.ui.callbacks.CheckMarkDialogCallback
import org.isoron.uhabits.core.ui.callbacks.NumberPickerCallback
import kotlin.math.roundToInt

@Inject
open class ListHabitsBehavior(
    private val habitList: HabitList,
    private val dirFinder: DirFinder,
    private val taskRunner: TaskRunner,
    private val screen: Screen,
    private val commandRunner: CommandRunner,
    private val prefs: Preferences,
    private val bugReporter: BugReporter
) {
    open fun onClickHabit(habitId: Long) {
        screen.showHabitScreen(habitId)
    }

    open fun onEdit(habitId: Long, date: LocalDate, x: Float, y: Float) {
        val habit = habitList.getById(habitId) ?: return
        val entry = habit.computedEntries.get(date)
        if (habit.type == HabitType.NUMERICAL) {
            val oldValue = entry.value.toDouble() / 1000
            screen.showNumberPopup(oldValue, entry.notes) { newValue: Double, newNotes: String ->
                val value = (newValue * 1000).roundToInt()
                if (newValue != oldValue && habit.targetType == AT_LEAST) {
                    screen.showConfetti(habit.color, x, y)
                }
                commandRunner.run(CreateRepetitionCommand(habitList, habit, date, value, newNotes))
            }
        } else {
            screen.showCheckmarkPopup(
                entry.value,
                entry.notes,
                habit.color
            ) { newValue: Int, newNotes: String ->
                if (newValue != entry.value && newValue == YES_MANUAL) screen.showConfetti(habit.color, x, y)
                commandRunner.run(CreateRepetitionCommand(habitList, habit, date, newValue, newNotes))
            }
        }
    }

    open fun onExportCSV() {
        val selected = habitList.toList()
        val outputDir = dirFinder.getCSVOutputDir()
        taskRunner.execute(
            ExportCSVTask(habitList, selected, outputDir) { filename: String? ->
                if (filename != null) {
                    screen.showSendFileScreen(filename)
                } else {
                    screen.showMessage(Message.COULD_NOT_EXPORT)
                }
            }
        )
    }

    open fun onFirstRun() {
        prefs.isFirstRun = false
        prefs.updateLastHint(-1, getToday())
        screen.showIntroScreen()
    }

    open fun reorderHabit(habitId: Long, refId: Long?, placeBefore: Boolean, targetCategory: String) {
        val habit = habitList.getById(habitId) ?: return
        val ref = refId?.let { habitList.getById(it) }
        val target = if (ref != null) {
            if (placeBefore) ReorderTarget.Before(ref) else ReorderTarget.After(ref)
        } else {
            null
        }
        commandRunner.run(ReorderHabitCommand(habitList, habit, target, targetCategory))
    }

    open fun onRenameCategory(category: String) {
        screen.showCategoryInput(category) { newCategory ->
            commandRunner.run(RenameCategoryCommand(habitList, category, newCategory))
        }
    }

    open fun onRepairDB() {
        taskRunner.execute(object : Task {
            override suspend fun doInBackground() {
                habitList.repair()
            }
            override fun onPostExecute() {
                screen.showMessage(Message.DATABASE_REPAIRED)
            }
        })
    }

    open fun onSendBugReport() {
        bugReporter.dumpBugReportToFile()
        try {
            val log = bugReporter.getBugReport()
            screen.showSendBugReportToDeveloperScreen(log)
        } catch (e: Exception) {
            e.printStackTrace()
            screen.showMessage(Message.COULD_NOT_GENERATE_BUG_REPORT)
        }
    }

    open fun onStartup() {
        prefs.incrementLaunchCount()
        if (prefs.isFirstRun) onFirstRun()
    }

    open fun onToggle(habitId: Long, date: LocalDate, value: Int, notes: String, x: Float, y: Float) {
        val habit = habitList.getById(habitId) ?: return
        commandRunner.run(
            CreateRepetitionCommand(habitList, habit, date, value, notes)
        )
        if (value == YES_MANUAL) screen.showConfetti(habit.color, x, y)
    }

    enum class Message {
        COULD_NOT_EXPORT,
        IMPORT_SUCCESSFUL,
        IMPORT_FAILED,
        DATABASE_REPAIRED,
        COULD_NOT_GENERATE_BUG_REPORT,
        FILE_NOT_RECOGNIZED
    }

    interface BugReporter {
        fun dumpBugReportToFile()

        fun getBugReport(): String
    }

    interface DirFinder {
        fun getCSVOutputDir(): UserFile
    }

    interface Screen {
        fun showCategoryInput(current: String, onConfirmed: (String) -> Unit)
        fun showHabitScreen(habitId: Long)
        fun showIntroScreen()
        fun showMessage(m: Message)
        fun showNumberPopup(
            value: Double,
            notes: String,
            callback: NumberPickerCallback
        )
        fun showCheckmarkPopup(
            selectedValue: Int,
            notes: String,
            color: PaletteColor,
            callback: CheckMarkDialogCallback
        )
        fun showSendBugReportToDeveloperScreen(log: String)
        fun showSendFileScreen(filename: String)
        fun showConfetti(color: PaletteColor, x: Float, y: Float)
    }
}
