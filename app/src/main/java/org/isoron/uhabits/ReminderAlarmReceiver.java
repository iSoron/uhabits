/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.models.Habit;

import java.util.Date;

public class ReminderAlarmReceiver extends BroadcastReceiver
{
    public static final String ACTION_CHECK = "org.isoron.uhabits.ACTION_CHECK";
    public static final String ACTION_DISMISS = "org.isoron.uhabits.ACTION_DISMISS";
    public static final String ACTION_REMIND = "org.isoron.uhabits.ACTION_REMIND";
    public static final String ACTION_REMOVE_REMINDER = "org.isoron.uhabits.ACTION_REMOVE_REMINDER";
    public static final String ACTION_SNOOZE = "org.isoron.uhabits.ACTION_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_REMIND:
                createNotification(context, intent.getData());
                break;

            case ACTION_DISMISS:
                dismissAllHabits();
                ReminderHelper.createReminderAlarms(context);
                break;

            case ACTION_CHECK:
                checkHabit(context, intent.getData());
                ReminderHelper.createReminderAlarms(context);
                break;

            case ACTION_SNOOZE:
                snoozeHabit(context, intent.getData());
                ReminderHelper.createReminderAlarms(context);
                break;
        }
    }

    private void snoozeHabit(Context context, Uri data)
    {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long delayMinutes = Long.parseLong(prefs.getString("pref_snooze_interval", "15"));

        Habit habit = Habit.get(ContentUris.parseId(data));
        ReminderHelper.createReminderAlarm(context, habit,
                new Date().getTime() + delayMinutes * 60 * 1000);
        dismissNotification(context, habit);
    }

    private void checkHabit(Context context, Uri data)
    {
        Long timestamp = DateHelper.getStartOfToday();
        String paramTimestamp = data.getQueryParameter("timestamp");

        if(paramTimestamp != null) timestamp = Long.parseLong(paramTimestamp);

        Habit habit = Habit.get(ContentUris.parseId(data));
        habit.toggleRepetition(timestamp);
        habit.save();
        dismissNotification(context, habit);
    }

    private void dismissAllHabits()
    {
        for (Habit h : Habit.getHighlightedHabits())
        {
            h.highlight = 0;
            h.save();
        }
    }

    private void dismissNotification(Context context, Habit habit)
    {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }


    private void createNotification(Context context, Uri data)
    {

        Habit habit = Habit.get(ContentUris.parseId(data));

        if (habit.hasImplicitRepToday()) return;

        habit.highlight = 1;
        habit.save();

        // Check if reminder has been turned off after alarm was scheduled
        if (habit.reminderHour == null) return;

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setData(data);
        PendingIntent contentPendingIntent =
                PendingIntent.getActivity(context, 0, contentIntent, 0);

        Intent deleteIntent = new Intent(context, ReminderAlarmReceiver.class);
        deleteIntent.setAction(ACTION_DISMISS);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

        Intent checkIntent = new Intent(context, ReminderAlarmReceiver.class);
        checkIntent.setData(data);
        checkIntent.setAction(ACTION_CHECK);
        PendingIntent checkIntentPending = PendingIntent.getBroadcast(context, 0, checkIntent, 0);

        Intent snoozeIntent = new Intent(context, ReminderAlarmReceiver.class);
        snoozeIntent.setData(data);
        snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent snoozeIntentPending = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender().setBackground(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.stripe));

        Notification notification =
                new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(habit.name)
                        .setContentText(habit.description)
                        .setContentIntent(contentPendingIntent)
                        .setDeleteIntent(deletePendingIntent)
                        .addAction(R.drawable.ic_action_check,
                                context.getString(R.string.check), checkIntentPending)
                        .addAction(R.drawable.ic_action_snooze,
                                context.getString(R.string.snooze), snoozeIntentPending)
                        .setSound(soundUri)
                        .extend(wearableExtender)
                        .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, notification);
    }

}
