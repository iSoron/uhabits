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
package org.isoron.uhabits.receivers

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfTodayWithOffset

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 *
 *
 * All broadcast messages are received and processed by this class.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action == null) return
        lastReceivedIntent = intent
        val app = context.applicationContext as HabitsApplication
        val appComponent = app.component
        val habits = appComponent.habitList
        val habitGroups = appComponent.habitGroupList
        val reminderController = appComponent.reminderController
        Log.i(TAG, String.format("Received intent: %s", intent.toString()))
        var habit: Habit? = null
        var habitGroup: HabitGroup? = null
        var id: Long? = null
        var type: String? = null
        val today: Long = getStartOfTodayWithOffset()
        val data = intent.data
        if (data != null) {
            type = data.pathSegments[0]
            id = ContentUris.parseId(data)
            when (type) {
                "habit" -> habit = habits.getById(id) ?: habitGroups.getHabitByID(id)
                "habitgroup" -> habitGroup = habitGroups.getById(id)
            }
        }
        val timestamp = intent.getLongExtra("timestamp", today)
        val reminderTime = intent.getLongExtra("reminderTime", today)
        try {
            when (intent.action) {
                ACTION_SHOW_REMINDER -> {
                    if (id == null) return
                    Log.d(
                        "ReminderReceiver",
                        String.format(
                            "onShowReminder %s=%d timestamp=%d reminderTime=%d",
                            type,
                            id,
                            timestamp,
                            reminderTime
                        )
                    )
                    if (habit != null) {
                        reminderController.onShowReminder(
                            habit,
                            Timestamp(timestamp),
                            reminderTime
                        )
                    } else {
                        reminderController.onShowReminder(
                            habitGroup!!,
                            Timestamp(timestamp),
                            reminderTime
                        )
                    }
                }
                ACTION_DISMISS_REMINDER -> {
                    if (id == null) return
                    Log.d("ReminderReceiver", String.format("onDismiss %s=%d", type, id))
                    if (habit != null) {
                        reminderController.onDismiss(habit)
                    } else {
                        reminderController.onDismiss(habitGroup!!)
                    }
                }
                ACTION_SNOOZE_REMINDER -> {
                    if (id == null) return
                    if (SDK_INT < Build.VERSION_CODES.S) {
                        Log.d(
                            "ReminderReceiver",
                            String.format("onSnoozePressed %s=%d", type, id)
                        )
                        if (habit != null) {
                            reminderController.onSnoozePressed(habit, context)
                        } else {
                            reminderController.onSnoozePressed(habitGroup!!, context)
                        }
                    } else {
                        Log.w(
                            "ReminderReceiver",
                            String.format(
                                "onSnoozePressed %s=%d, should be deactivated in recent versions.",
                                type,
                                id
                            )
                        )
                    }
                }
                Intent.ACTION_BOOT_COMPLETED -> {
                    Log.d("ReminderReceiver", "onBootCompleted")
                    reminderController.onBootCompleted()
                }
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "could not process intent", e)
        }
    }

    companion object {
        const val ACTION_DISMISS_REMINDER = "org.isoron.uhabits.ACTION_DISMISS_REMINDER"
        const val ACTION_SHOW_REMINDER = "org.isoron.uhabits.ACTION_SHOW_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "org.isoron.uhabits.ACTION_SNOOZE_REMINDER"
        private const val TAG = "ReminderReceiver"
        var lastReceivedIntent: Intent? = null
            private set

        fun clearLastReceivedIntent() {
            lastReceivedIntent = null
        }
    }
}
