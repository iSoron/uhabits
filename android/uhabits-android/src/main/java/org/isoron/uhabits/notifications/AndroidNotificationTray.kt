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

package org.isoron.uhabits.notifications

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.BitmapFactory.*
import android.os.*
import android.os.Build.VERSION.*
import androidx.core.app.*
import androidx.core.app.NotificationCompat.*
import android.util.*
import org.isoron.androidbase.*
import org.isoron.uhabits.R
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.intents.*
import javax.inject.*

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


    override fun removeNotification(id: Int) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(id)
        active.remove(id)

        // Clear the group summary notification
        if (active.isEmpty()) manager.cancelAll()
    }

    override fun showNotification(habit: Habit,
                                  notificationId: Int,
                                  timestamp: Timestamp,
                                  reminderTime: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        //val summary = buildSummary(habit, reminderTime)
        //notificationManager.notify(Int.MAX_VALUE, summary)
        val notification = buildNotification(habit, reminderTime, timestamp)
        createAndroidNotificationChannel(context)
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: RuntimeException) {
            // Some Xiaomi phones produce a RuntimeException if custom notification sounds are used.
            Log.i("AndroidNotificationTray",
                  "Failed to show notification. Retrying without sound.")
            val n = buildNotification(habit,
                                      reminderTime,
                                      timestamp,
                                      disableSound = true)
            notificationManager.notify(notificationId, n)

        }
        active.add(notificationId)
    }

    fun buildNotification(habit: Habit,
                          reminderTime: Long,
                          timestamp: Timestamp,
                          disableSound: Boolean = false): Notification {

        val addRepetitionAction = Action(
                R.drawable.ic_action_check,
                context.getString(R.string.yes),
                pendingIntents.addCheckmark(habit, timestamp))

        val removeRepetitionAction = Action(
                R.drawable.ic_action_cancel,
                context.getString(R.string.no),
                pendingIntents.removeRepetition(habit))

        val wearableBg = decodeResource(context.resources, R.drawable.stripe)

        // Even though the set of actions is the same on the phone and
        // on the watch, Pebble requires us to add them to the
        // WearableExtender.
        val wearableExtender = WearableExtender()
                .setBackground(wearableBg)
                .addAction(addRepetitionAction)
                .addAction(removeRepetitionAction)

        val defaultText = context.getString(R.string.default_reminder_question)
        val builder = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(habit.name)
                .setContentText(if(habit.description.isBlank()) defaultText else habit.description)
                .setContentIntent(pendingIntents.showHabit(habit))
                .setDeleteIntent(pendingIntents.dismissNotification(habit))
                .addAction(addRepetitionAction)
                .addAction(removeRepetitionAction)
                .setSound(null)
                .setWhen(reminderTime)
                .setShowWhen(true)
                .setOngoing(preferences.shouldMakeNotificationsSticky())
                .setGroup("group" + habit.getId())

        if (!disableSound)
            builder.setSound(ringtoneManager.getURI())

        if (preferences.shouldMakeNotificationsLed())
            builder.setLights(Color.RED, 1000, 1000)

        val snoozeAction = Action(R.drawable.ic_action_snooze,
                                  context.getString(R.string.snooze),
                                  pendingIntents.snoozeNotification(habit))
        wearableExtender.addAction(snoozeAction)
        builder.addAction(snoozeAction)

        builder.extend(wearableExtender)
        return builder.build()
    }

    private fun buildSummary(habit: Habit,
                             reminderTime: Long): Notification {
        return NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(reminderTime)
                .setShowWhen(true)
                .setGroup("group" + habit.getId())
                .setGroupSummary(true)
                .build()
    }

    companion object {
        private const val REMINDERS_CHANNEL_ID = "REMINDERS"
        fun createAndroidNotificationChannel(context: Context) {
            val notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE)
                    as NotificationManager
            if (SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(REMINDERS_CHANNEL_ID,
                                                  context.resources.getString(R.string.reminder),
                                                  NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

}
