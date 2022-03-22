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

package org.isoron.uhabits.notifications

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory.decodeResource
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.WearableExtender
import androidx.core.app.NotificationManagerCompat
import org.isoron.uhabits.R
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.NotificationTray
import org.isoron.uhabits.inject.AppContext
import org.isoron.uhabits.intents.PendingIntentFactory
import javax.inject.Inject

@AppScope
class AndroidNotificationTray
@Inject constructor(
    @AppContext private val context: Context,
    private val pendingIntents: PendingIntentFactory,
    private val preferences: Preferences,
    private val ringtoneManager: RingtoneManager
) : NotificationTray.SystemTray {
    private var active = HashSet<Int>()

    override fun log(msg: String) {
        Log.d("AndroidNotificationTray", msg)
    }

    override fun removeNotification(notificationId: Int) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(notificationId)
        active.remove(notificationId)
    }

    override fun showNotification(
        habit: Habit,
        notificationId: Int,
        timestamp: Timestamp,
        reminderTime: Long
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notification = buildNotification(habit, reminderTime, timestamp)
        createAndroidNotificationChannel(context)
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: RuntimeException) {
            // Some Xiaomi phones produce a RuntimeException if custom notification sounds are used.
            Log.i(
                "AndroidNotificationTray",
                "Failed to show notification. Retrying without sound."
            )
            val n = buildNotification(
                habit,
                reminderTime,
                timestamp,
                disableSound = true
            )
            notificationManager.notify(notificationId, n)
        }
        active.add(notificationId)
    }

    fun buildNotification(
        habit: Habit,
        reminderTime: Long,
        timestamp: Timestamp,
        disableSound: Boolean = false
    ): Notification {

        val addRepetitionAction = Action(
            R.drawable.ic_action_check,
            context.getString(R.string.yes),
            pendingIntents.addCheckmark(habit, timestamp)
        )

        val removeRepetitionAction = Action(
            R.drawable.ic_action_cancel,
            context.getString(R.string.no),
            pendingIntents.removeRepetition(habit, timestamp)
        )

        val enterAction = Action(
            R.drawable.ic_action_check,
            context.getString(R.string.enter),
            pendingIntents.setNumericalValue(context, habit, 0, null)
        )

        val wearableBg = decodeResource(context.resources, R.drawable.stripe)

        // Even though the set of actions is the same on the phone and
        // on the watch, Pebble requires us to add them to the
        // WearableExtender.
        val wearableExtender = WearableExtender().setBackground(wearableBg)

        val defaultText = context.getString(R.string.default_reminder_question)
        val builder = Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habit.name)
            .setContentText(if (habit.question.isBlank()) defaultText else habit.question)
            .setContentIntent(pendingIntents.showHabit(habit))
            .setDeleteIntent(pendingIntents.dismissNotification(habit))
            .setSound(null)
            .setWhen(reminderTime)
            .setShowWhen(true)
            .setOngoing(preferences.shouldMakeNotificationsSticky())

        if (habit.isNumerical) {
            wearableExtender.addAction(enterAction)
            builder.addAction(enterAction)
        } else {
            wearableExtender
                .addAction(addRepetitionAction)
                .addAction(removeRepetitionAction)
            builder
                .addAction(addRepetitionAction)
                .addAction(removeRepetitionAction)
        }

        if (!disableSound)
            builder.setSound(ringtoneManager.getURI())

        if (SDK_INT < Build.VERSION_CODES.S) {
            val snoozeAction = Action(
                R.drawable.ic_action_snooze,
                context.getString(R.string.snooze),
                pendingIntents.snoozeNotification(habit)
            )
            wearableExtender.addAction(snoozeAction)
            builder.addAction(snoozeAction)
        }

        builder.extend(wearableExtender)
        return builder.build()
    }

    companion object {
        private const val REMINDERS_CHANNEL_ID = "REMINDERS"
        fun createAndroidNotificationChannel(context: Context) {
            val notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE)
                as NotificationManager
            if (SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    REMINDERS_CHANNEL_ID,
                    context.resources.getString(R.string.reminder),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
