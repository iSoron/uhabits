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
package org.isoron.uhabits.core.ui

import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.ArchiveHabitsCommand
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.commands.EditHabitCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner

@AppScope
@Inject
open class NotificationTray(
    private val taskRunner: TaskRunner,
    private val commandRunner: CommandRunner,
    private val preferences: Preferences,
    private val systemTray: SystemTray
) : CommandRunner.Listener, Preferences.Listener {
    private val active: MutableMap<Habit, NotificationData> = mutableMapOf()
    open fun cancel(habit: Habit) {
        val notificationId = getNotificationId(habit)
        systemTray.removeNotification(notificationId)
        // Habit's equals/hashCode depend on mutable fields (e.g. isArchived, reminder),
        // so a key lookup may miss after the habit has been modified. Remove by id instead.
        active.keys.removeAll { it === habit || (habit.id != null && it.id == habit.id) }
    }

    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) {
            val (_, habit) = command
            cancel(habit)
        }
        if (command is DeleteHabitsCommand) {
            val (_, deleted) = command
            for (habit in deleted) cancel(habit)
        }
        if (command is ArchiveHabitsCommand) {
            val (_, archived) = command
            for (habit in archived) cancel(habit)
        }
        if (command is EditHabitCommand) {
            val (habitList, habitId, modified) = command
            if (!modified.hasReminder()) {
                habitList.getById(habitId)?.let { cancel(it) }
            }
        }
    }

    override fun onNotificationsChanged() {
        reshowAll()
    }

    open fun show(habit: Habit, date: LocalDate, reminderTime: Long) {
        val data = NotificationData(date, reminderTime)
        active[habit] = data
        taskRunner.execute(ShowNotificationTask(habit, data))
    }

    open fun startListening() {
        commandRunner.addListener(this)
        preferences.addListener(this)
    }

    open fun stopListening() {
        commandRunner.removeListener(this)
        preferences.removeListener(this)
    }

    private fun getNotificationId(habit: Habit): Int {
        val id = habit.id ?: return 0
        return (id % Int.MAX_VALUE).toInt()
    }

    private fun reshowAll() {
        for ((habit, data) in active.entries) {
            taskRunner.execute(ShowNotificationTask(habit, data))
        }
    }

    open fun reshow(habit: Habit) {
        active[habit]?.let {
            taskRunner.execute(ShowNotificationTask(habit, it))
        }
    }

    interface SystemTray {
        fun removeNotification(notificationId: Int)
        fun showNotification(
            habit: Habit,
            notificationId: Int,
            date: LocalDate,
            reminderTime: Long
        )

        fun log(msg: String)
    }

    internal class NotificationData(val date: LocalDate, val reminderTime: Long)
    private inner class ShowNotificationTask(private val habit: Habit, data: NotificationData) :
        Task {
        var isCompleted = false
        private val date: LocalDate = data.date
        private val reminderTime: Long = data.reminderTime

        override suspend fun doInBackground() {
            isCompleted = habit.isCompletedToday()
        }

        override fun onPostExecute() {
            systemTray.log("Showing notification for habit=" + habit.id)
            if (isCompleted && habit.targetType != NumericalHabitType.AT_MOST) {
                systemTray.log("Habit ${habit.id} already checked. Skipping.")
                return
            }
            if (!habit.hasReminder()) {
                systemTray.log("Habit ${habit.id} does not have a reminder. Skipping.")
                return
            }
            if (habit.isArchived) {
                systemTray.log("Habit ${habit.id} is archived. Skipping.")
                return
            }
            if (!shouldShowReminderToday()) {
                systemTray.log("Habit ${habit.id} not supposed to run today. Skipping.")
                return
            }
            systemTray.showNotification(
                habit,
                getNotificationId(habit),
                date,
                reminderTime
            )
        }

        private fun shouldShowReminderToday(): Boolean {
            if (!habit.hasReminder()) return false
            val reminder = habit.reminder
            val reminderDays = reminder!!.days.toArray()
            val weekday = (date.dayOfWeek.daysSinceSunday + 1) % 7
            return reminderDays[weekday]
        }
    }

    companion object {
        const val REMINDERS_CHANNEL_ID = "REMINDERS"
    }
}
