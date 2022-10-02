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
package org.isoron.uhabits.core.ui

import kotlinx.serialization.Serializable
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner
import java.util.Locale
import java.util.Objects
import javax.inject.Inject

@AppScope
class NotificationTray @Inject constructor(
    private val taskRunner: TaskRunner,
    private val commandRunner: CommandRunner,
    private val preferences: Preferences,
    private val systemTray: SystemTray,
    private val habitList: HabitList
) : CommandRunner.Listener, Preferences.Listener {

    /**
     * A mapping from habits to active notifications, automatically persisting on removal.
     */
    private val active = object {
        private val m: HashMap<Habit, NotificationData> =
            preferences.getActiveNotifications(habitList)

        val entries get() = m.entries

        operator fun set(habit: Habit, notificationData: NotificationData) {
            m[habit] = notificationData
            persist()
        }

        fun remove(habit: Habit) {
            m.remove(habit)?.let { persist() } // persist if changed
        }

        fun persist() = preferences.setActiveNotifications(m)
    }

    fun cancel(habit: Habit) {
        val notificationId = getNotificationId(habit)
        systemTray.removeNotification(notificationId)
        active.remove(habit)
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
    }

    override fun onNotificationsChanged() {
        reshowAll()
    }

    fun show(habit: Habit, timestamp: Timestamp, reminderTime: Long) {
        val data = NotificationData(timestamp, reminderTime)
        taskRunner.execute(ShowNotificationTask(habit, data, silent = false))
    }

    fun startListening() {
        commandRunner.addListener(this)
        preferences.addListener(this)
    }

    fun stopListening() {
        commandRunner.removeListener(this)
        preferences.removeListener(this)
    }

    private fun getNotificationId(habit: Habit): Int {
        val id = habit.id ?: return 0
        return (id % Int.MAX_VALUE).toInt()
    }

    fun reshowAll() {
        for ((habit, data) in active.entries) {
            taskRunner.execute(ShowNotificationTask(habit, data, silent = true))
        }
    }

    interface SystemTray {
        fun removeNotification(notificationId: Int)
        fun showNotification(
            habit: Habit,
            notificationId: Int,
            timestamp: Timestamp,
            reminderTime: Long,
            silent: Boolean = false
        )

        fun log(msg: String)
    }

    @Serializable
    internal data class NotificationData(
        val timestamp: Timestamp,
        val reminderTime: Long,
    )

    private inner class ShowNotificationTask(
        private val habit: Habit,
        private val data: NotificationData,
        private val silent: Boolean
    ) :
        Task {
        var isCompleted = false

        override fun doInBackground() {
            isCompleted = habit.isCompletedToday()
        }

        override fun onPostExecute() {
            systemTray.log("Showing notification for habit=" + habit.id)
            if (isCompleted) {
                systemTray.log(
                    String.format(
                        Locale.US,
                        "Habit %d already checked. Skipping.",
                        habit.id
                    )
                )
                active.remove(habit)
                return
            }
            if (!habit.hasReminder()) {
                systemTray.log(
                    String.format(
                        Locale.US,
                        "Habit %d does not have a reminder. Skipping.",
                        habit.id
                    )
                )
                active.remove(habit)
                return
            }
            if (habit.isArchived) {
                systemTray.log(
                    String.format(
                        Locale.US,
                        "Habit %d is archived. Skipping.",
                        habit.id
                    )
                )
                active.remove(habit)
                return
            }
            if (!shouldShowReminderToday()) {
                systemTray.log(
                    String.format(
                        Locale.US,
                        "Habit %d not supposed to run today. Skipping.",
                        habit.id
                    )
                )
                active.remove(habit)
                return
            }
            systemTray.showNotification(
                habit,
                getNotificationId(habit),
                data.timestamp,
                data.reminderTime,
                silent = silent
            )
            if (silent) {
                systemTray.log(
                    String.format(
                        Locale.US,
                        "Showing notification for habit %d silently because it has been shown before.",
                        habit.id
                    )
                )
            }
            active[habit] = data
        }

        private fun shouldShowReminderToday(): Boolean {
            if (!habit.hasReminder()) return false
            val reminder = habit.reminder
            val reminderDays = Objects.requireNonNull(reminder)!!.days.toArray()
            val weekday = data.timestamp.weekday
            return reminderDays[weekday]
        }
    }

    companion object {
        const val REMINDERS_CHANNEL_ID = "REMINDERS"
    }
}
