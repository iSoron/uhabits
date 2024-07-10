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
package org.isoron.uhabits.core.reminders

import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.ChangeHabitColorCommand
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.applyTimezone
import org.isoron.uhabits.core.utils.DateUtils.Companion.getLocalTime
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfDayWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.removeTimezone
import java.util.Locale
import java.util.Objects
import javax.inject.Inject

@AppScope
class ReminderScheduler @Inject constructor(
    private val commandRunner: CommandRunner,
    private val habitList: HabitList,
    private val habitGroupList: HabitGroupList,
    private val sys: SystemScheduler,
    private val widgetPreferences: WidgetPreferences
) : CommandRunner.Listener {
    @Synchronized
    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) return
        if (command is ChangeHabitColorCommand) return
        scheduleAll()
    }

    @Synchronized
    fun schedule(habit: Habit) {
        if (habit.id == null) {
            sys.log("ReminderScheduler", "Habit has null id. Returning.")
            return
        }
        if (!habit.hasReminder()) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.")
            return
        }
        var reminderTime = Objects.requireNonNull(habit.reminder)!!.timeInMillis
        val snoozeReminderTime = widgetPreferences.getSnoozeTime(habit.id!!)
        if (snoozeReminderTime != 0L) {
            val now = applyTimezone(getLocalTime())
            sys.log(
                "ReminderScheduler",
                String.format(
                    Locale.US,
                    "Habit %d has been snoozed until %d",
                    habit.id,
                    snoozeReminderTime
                )
            )
            if (snoozeReminderTime > now) {
                sys.log("ReminderScheduler", "Snooze time is in the future. Accepting.")
                reminderTime = snoozeReminderTime
            } else {
                sys.log("ReminderScheduler", "Snooze time is in the past. Discarding.")
                widgetPreferences.removeSnoozeTime(habit.id!!)
            }
        }
        scheduleAtTime(habit, reminderTime)
    }

    @Synchronized
    fun schedule(habitGroup: HabitGroup) {
        if (habitGroup.id == null) {
            sys.log("ReminderScheduler", "Habit group has null id. Returning.")
            return
        }
        if (!habitGroup.hasReminder()) {
            sys.log("ReminderScheduler", "habit group=" + habitGroup.id + " has no reminder. Skipping.")
            return
        }
        var reminderTime = Objects.requireNonNull(habitGroup.reminder)!!.timeInMillis
        val snoozeReminderTime = widgetPreferences.getSnoozeTime(habitGroup.id!!)
        if (snoozeReminderTime != 0L) {
            val now = applyTimezone(getLocalTime())
            sys.log(
                "ReminderScheduler",
                String.format(
                    Locale.US,
                    "Habit group %d has been snoozed until %d",
                    habitGroup.id,
                    snoozeReminderTime
                )
            )
            if (snoozeReminderTime > now) {
                sys.log("ReminderScheduler", "Snooze time is in the future. Accepting.")
                reminderTime = snoozeReminderTime
            } else {
                sys.log("ReminderScheduler", "Snooze time is in the past. Discarding.")
                widgetPreferences.removeSnoozeTime(habitGroup.id!!)
            }
        }
        scheduleAtTime(habitGroup, reminderTime)
    }

    @Synchronized
    fun scheduleAtTime(habit: Habit, reminderTime: Long) {
        sys.log("ReminderScheduler", "Scheduling alarm for habit=" + habit.id)
        if (!habit.hasReminder()) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.")
            return
        }
        if (habit.isArchived) {
            sys.log("ReminderScheduler", "habit=" + habit.id + " is archived. Skipping.")
            return
        }
        val timestamp = getStartOfDayWithOffset(removeTimezone(reminderTime))
        sys.log(
            "ReminderScheduler",
            String.format(
                Locale.US,
                "reminderTime=%d removeTimezone=%d timestamp=%d",
                reminderTime,
                removeTimezone(reminderTime),
                timestamp
            )
        )
        sys.scheduleShowReminder(reminderTime, habit, timestamp)
    }

    @Synchronized
    fun scheduleAtTime(habitGroup: HabitGroup, reminderTime: Long) {
        sys.log("ReminderScheduler", "Scheduling alarm for habit group=" + habitGroup.id)
        if (!habitGroup.hasReminder()) {
            sys.log("ReminderScheduler", "habit group=" + habitGroup.id + " has no reminder. Skipping.")
            return
        }
        if (habitGroup.isArchived) {
            sys.log("ReminderScheduler", "habit group=" + habitGroup.id + " is archived. Skipping.")
            return
        }
        val timestamp = getStartOfDayWithOffset(removeTimezone(reminderTime))
        sys.log(
            "ReminderScheduler",
            String.format(
                Locale.US,
                "reminderTime=%d removeTimezone=%d timestamp=%d",
                reminderTime,
                removeTimezone(reminderTime),
                timestamp
            )
        )
        sys.scheduleShowReminder(reminderTime, habitGroup, timestamp)
    }

    @Synchronized
    fun scheduleAll() {
        sys.log("ReminderScheduler", "Scheduling all alarms")
        val reminderHabits = habitList.getFiltered(HabitMatcher.WITH_ALARM)
        val reminderSubHabits = habitGroupList.map { it.habitList.getFiltered(HabitMatcher.WITH_ALARM) }.flatten()
        val reminderHabitGroups = habitGroupList.getFiltered(HabitMatcher.WITH_ALARM)
        for (habit in reminderHabits) schedule(habit)
        for (habit in reminderSubHabits) schedule(habit)
        for (hgr in reminderHabitGroups) schedule(hgr)
    }

    @Synchronized
    fun hasHabitsWithReminders(): Boolean {
        if (!habitList.getFiltered(HabitMatcher.WITH_ALARM).isEmpty) return true
        if (habitGroupList.map { it.habitList.getFiltered(HabitMatcher.WITH_ALARM) }.flatten().isNotEmpty()) return true
        if (!habitGroupList.getFiltered(HabitMatcher.WITH_ALARM).isEmpty) return true
        return false
    }

    @Synchronized
    fun startListening() {
        commandRunner.addListener(this)
    }

    @Synchronized
    fun stopListening() {
        commandRunner.removeListener(this)
    }

    @Synchronized
    fun snoozeReminder(habit: Habit, minutes: Long) {
        val now = applyTimezone(getLocalTime())
        val snoozedUntil = now + minutes * 60 * 1000
        widgetPreferences.setSnoozeTime(habit.id!!, snoozedUntil)
        schedule(habit)
    }

    @Synchronized
    fun snoozeReminder(habitGroup: HabitGroup, minutes: Long) {
        val now = applyTimezone(getLocalTime())
        val snoozedUntil = now + minutes * 60 * 1000
        widgetPreferences.setSnoozeTime(habitGroup.id!!, snoozedUntil)
        schedule(habitGroup)
    }

    interface SystemScheduler {
        fun scheduleShowReminder(
            reminderTime: Long,
            habit: Habit,
            timestamp: Long
        ): SchedulerResult

        fun scheduleShowReminder(
            reminderTime: Long,
            habitGroup: HabitGroup,
            timestamp: Long
        ): SchedulerResult

        fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult?
        fun log(componentName: String, msg: String)
    }

    enum class SchedulerResult {
        IGNORED, OK
    }
}
