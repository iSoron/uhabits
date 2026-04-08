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
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteHabitGroupsCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner

@AppScope
@Inject
class NotificationTray(
    private val taskRunner: TaskRunner,
    private val commandRunner: CommandRunner,
    private val preferences: Preferences,
    private val systemTray: SystemTray
) : CommandRunner.Listener, Preferences.Listener {
    private val activeHabits: MutableMap<Habit, NotificationData> = mutableMapOf()
    private val activeHabitGroups: MutableMap<HabitGroup, NotificationData> = mutableMapOf()
    fun cancel(habit: Habit) {
        val notificationId = getNotificationId(habit)
        systemTray.removeNotification(notificationId)
        activeHabits.remove(habit)
    }

    fun cancel(habitGroup: HabitGroup) {
        val notificationId = getNotificationId(habitGroup)
        systemTray.removeNotification(notificationId)
        activeHabitGroups.remove(habitGroup)
    }

    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) {
            val (_, habit) = command
            cancel(habit)

            // Also dismiss the parent HabitGroup's notification if one is active
            habit.groupId?.let { groupId ->
                activeHabitGroups.keys.find { it.id == groupId }?.let { group ->
                    cancel(group)
                }
            }
        }
        if (command is DeleteHabitsCommand) {
            val (_, deleted) = command
            for (habit in deleted) cancel(habit)
        }
        if (command is DeleteHabitGroupsCommand) {
            val (_, deletedGroups) = command
            for (hgr in deletedGroups) {
                for (h in hgr.habitList) cancel(h)
                cancel(hgr)
            }
        }
    }

    override fun onNotificationsChanged() {
        reshowAll()
    }

    fun show(habit: Habit, date: LocalDate, reminderTime: Long) {
        val data = NotificationData(date, reminderTime)
        activeHabits[habit] = data
        taskRunner.execute(ShowNotificationTask(habit, data))
    }

    fun show(habitGroup: HabitGroup, date: LocalDate, reminderTime: Long) {
        val data = NotificationData(date, reminderTime)
        activeHabitGroups[habitGroup] = data
        taskRunner.execute(ShowNotificationTask(habitGroup, data))
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

    private fun getNotificationId(habitGroup: HabitGroup): Int {
        val id = habitGroup.id ?: return 0
        return (id % Int.MAX_VALUE).toInt()
    }

    private fun reshowAll() {
        for ((habit, data) in activeHabits.entries) {
            taskRunner.execute(ShowNotificationTask(habit, data))
        }
        for ((habitGroup, data) in activeHabitGroups.entries) {
            taskRunner.execute(ShowNotificationTask(habitGroup, data))
        }
    }

    fun reshow(habit: Habit) {
        activeHabits[habit]?.let {
            taskRunner.execute(ShowNotificationTask(habit, it))
        }
    }

    fun reshow(habitGroup: HabitGroup) {
        activeHabitGroups[habitGroup]?.let {
            taskRunner.execute(ShowNotificationTask(habitGroup, it))
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

        fun showNotification(
            habitGroup: HabitGroup,
            notificationId: Int,
            date: LocalDate,
            reminderTime: Long
        )

        fun log(msg: String)
    }

    internal class NotificationData(val date: LocalDate, val reminderTime: Long)
    private inner class ShowNotificationTask private constructor(
        private val habit: Habit? = null,
        private val habitGroup: HabitGroup? = null,
        data: NotificationData
    ) : Task {
        // Secondary constructor for Habit
        constructor(habit: Habit, data: NotificationData) : this(habit, null, data)

        // Secondary constructor for HabitGroup
        constructor(habitGroup: HabitGroup, data: NotificationData) : this(null, habitGroup, data)

        var isCompleted = false
        private val date: LocalDate = data.date
        private val reminderTime: Long = data.reminderTime

        private val type = if (habit != null) "Habit" else "HabitGroup"
        private val id = habit?.id ?: habitGroup?.id
        private val hasReminder = habit?.hasReminder() ?: habitGroup!!.hasReminder()
        private val isArchived = habit?.isArchived ?: habitGroup!!.isArchived
        private val isAtleastHabit = if (habit != null) {
            habit.targetType != NumericalHabitType.AT_MOST
        } else {
            true
        }

        override fun doInBackground() {
            isCompleted = habit?.isCompletedToday() ?: habitGroup!!.isCompletedToday()
        }

        override fun onPostExecute() {
            systemTray.log("Showing notification for $type = $id")
            if (isCompleted && isAtleastHabit) {
                systemTray.log("$type $id already checked. Skipping.")
                return
            }
            if (!hasReminder) {
                systemTray.log("$type $id does not have a reminder. Skipping.")
                return
            }
            if (isArchived) {
                systemTray.log("$type $id is archived. Skipping.")
                return
            }
            if (!shouldShowReminderToday()) {
                systemTray.log("$type $id not supposed to run today. Skipping.")
                return
            }
            if (habit != null) {
                systemTray.showNotification(
                    habit,
                    getNotificationId(habit),
                    date,
                    reminderTime
                )
            } else {
                systemTray.showNotification(
                    habitGroup!!,
                    getNotificationId(habitGroup),
                    date,
                    reminderTime
                )
            }
        }

        private fun shouldShowReminderToday(): Boolean {
            if (!hasReminder) return false
            val reminder = habit?.reminder ?: habitGroup!!.reminder
            val reminderDays = reminder!!.days.toArray()
            val weekday = (date.dayOfWeek.daysSinceSunday + 1) % 7
            return reminderDays[weekday]
        }
    }

    companion object {
        const val REMINDERS_CHANNEL_ID = "REMINDERS"
    }
}
