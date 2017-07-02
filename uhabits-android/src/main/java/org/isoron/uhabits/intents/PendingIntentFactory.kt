/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.intents

import android.app.*
import android.app.PendingIntent.*
import android.content.*
import android.net.*
import org.isoron.androidbase.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.receivers.*
import javax.inject.*

@AppScope
class PendingIntentFactory
@Inject constructor(
        @AppContext private val context: Context,
        private val intentFactory: IntentFactory) {

    fun addCheckmark(habit: Habit, timestamp: Long?): PendingIntent =
            PendingIntent.getBroadcast(
                    context, 1,
                    Intent(context, WidgetReceiver::class.java).apply {
                        data = Uri.parse(habit.uriString)
                        action = WidgetReceiver.ACTION_ADD_REPETITION
                        if (timestamp != null) putExtra("timestamp", timestamp)
                    },
                    FLAG_UPDATE_CURRENT)

    fun dismissNotification(habit: Habit): PendingIntent =
            PendingIntent.getBroadcast(
                    context, 0,
                    Intent(context, ReminderReceiver::class.java).apply {
                        action = WidgetReceiver.ACTION_DISMISS_REMINDER
                        data = Uri.parse(habit.uriString)
                    },
                    FLAG_UPDATE_CURRENT)

    fun showHabit(habit: Habit): PendingIntent =
            android.support.v4.app.TaskStackBuilder
                    .create(context)
                    .addNextIntentWithParentStack(
                            intentFactory.startShowHabitActivity(
                                    context, habit))
                    .getPendingIntent(0, FLAG_UPDATE_CURRENT)

    fun showReminder(habit: Habit,
                     reminderTime: Long?,
                     timestamp: Long): PendingIntent =
            PendingIntent.getBroadcast(
                    context,
                    (habit.getId()!! % Integer.MAX_VALUE).toInt() + 1,
                    Intent(context, ReminderReceiver::class.java).apply {
                        action = ReminderReceiver.ACTION_SHOW_REMINDER
                        data = Uri.parse(habit.uriString)
                        putExtra("timestamp", timestamp)
                        putExtra("reminderTime", reminderTime)
                    },
                    FLAG_UPDATE_CURRENT)

    fun snoozeNotification(habit: Habit): PendingIntent =
            PendingIntent.getBroadcast(
                    context, 0,
                    Intent(context, ReminderReceiver::class.java).apply {
                        data = Uri.parse(habit.uriString)
                        action = ReminderReceiver.ACTION_SNOOZE_REMINDER
                    },
                    FLAG_UPDATE_CURRENT)

    fun toggleCheckmark(habit: Habit, timestamp: Long?): PendingIntent =
            PendingIntent.getBroadcast(
                    context, 2,
                    Intent(context, WidgetReceiver::class.java).apply {
                        data = Uri.parse(habit.uriString)
                        action = WidgetReceiver.ACTION_TOGGLE_REPETITION
                        if (timestamp != null) putExtra("timestamp", timestamp)
                    },
                    FLAG_UPDATE_CURRENT)
}
