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
import android.support.annotation.*
import android.support.v4.app.*
import android.support.v4.app.NotificationCompat.*
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

    override fun removeNotification(id: Int) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(id)
        active.remove(id)

        // Clear the group summary notification
        if(active.isEmpty()) manager.cancelAll()
    }

    override fun showNotification(habit: Habit,
                                  notificationId: Int,
                                  timestamp: Timestamp,
                                  reminderTime: Long)
    {
        val notificationManager = NotificationManagerCompat.from(context)
        val summary = buildSummary(reminderTime)
        notificationManager.notify(Int.MAX_VALUE, summary)
        val notification = buildNotification(habit, reminderTime, timestamp)
        notificationManager.notify(notificationId, notification)
        active.add(notificationId)
    }

    @NonNull
    fun buildNotification(@NonNull habit: Habit,
                          @NonNull reminderTime: Long,
                          @NonNull timestamp: Timestamp) : Notification
    {

        val addRepetitionAction = Action(
                R.drawable.ic_action_check,
                context.getString(R.string.yes),
                pendingIntents.addCheckmark(habit, timestamp))

        val snoozeAction = Action(
                R.drawable.ic_action_snooze,
                context.getString(R.string.snooze),
                pendingIntents.snoozeNotification(habit))

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
                .addAction(snoozeAction)

        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(habit.name)
                .setContentText(habit.description)
                .setContentIntent(pendingIntents.showHabit(habit))
                .setDeleteIntent(pendingIntents.dismissNotification(habit))
                .addAction(addRepetitionAction)
                .addAction(removeRepetitionAction)
                .addAction(snoozeAction)
                .setSound(ringtoneManager.getURI())
                .extend(wearableExtender)
                .setWhen(reminderTime)
                .setShowWhen(true)
                .setOngoing(preferences.shouldMakeNotificationsSticky())
		        .setGroup("default")

        if (preferences.shouldMakeNotificationsLed())
            builder.setLights(Color.RED, 1000, 1000)

	return builder.build()
    }

    @NonNull
    private fun buildSummary(@NonNull reminderTime: Long) : Notification
    {
        return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(reminderTime)
                .setShowWhen(true)
                .setGroup("default")
                .setGroupSummary(true)
                .build()
    }
}
