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
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.ReminderUtils;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.ui.habits.show.ShowHabitActivity;
import org.isoron.uhabits.widgets.WidgetManager;

import java.util.Date;

public class HabitBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_CHECK = "org.isoron.uhabits.ACTION_CHECK";
    public static final String ACTION_DISMISS = "org.isoron.uhabits.ACTION_DISMISS";
    public static final String ACTION_SHOW_REMINDER = "org.isoron.uhabits.ACTION_SHOW_REMINDER";
    public static final String ACTION_SNOOZE = "org.isoron.uhabits.ACTION_SNOOZE";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_SHOW_REMINDER:
                createNotification(context, intent);
                createReminderAlarmsDelayed(context);
                break;

            case ACTION_DISMISS:
                dismissAllHabits();
                break;

            case ACTION_CHECK:
                checkHabit(context, intent);
                break;

            case ACTION_SNOOZE:
                snoozeHabit(context, intent);
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                ReminderUtils.createReminderAlarms(context);
                break;
        }
    }

    private void createReminderAlarmsDelayed(final Context context)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                ReminderUtils.createReminderAlarms(context);
            }
        }, 5000);
    }

    private void snoozeHabit(Context context, Intent intent)
    {
        Uri data = intent.getData();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long delayMinutes = Long.parseLong(prefs.getString("pref_snooze_interval", "15"));

        long habitId = ContentUris.parseId(data);
        Habit habit = Habit.get(habitId);
        if(habit != null)
            ReminderUtils.createReminderAlarm(context, habit,
                    new Date().getTime() + delayMinutes * 60 * 1000);
        dismissNotification(context, habitId);
    }

    private void checkHabit(Context context, Intent intent)
    {
        Uri data = intent.getData();
        Long timestamp = intent.getLongExtra("timestamp", DateUtils.getStartOfToday());

        long habitId = ContentUris.parseId(data);
        Habit habit = Habit.get(habitId);
        if(habit != null)
            habit.repetitions.toggle(timestamp);
        dismissNotification(context, habitId);

        sendRefreshBroadcast(context);
    }

    public static void sendRefreshBroadcast(Context context)
    {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        Intent refreshIntent = new Intent(HabitsApplication.ACTION_REFRESH);
        manager.sendBroadcast(refreshIntent);

        WidgetManager.updateWidgets(context);
    }

    private void dismissAllHabits()
    {

    }

    private void dismissNotification(Context context, Long habitId)
    {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habitId % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }


    private void createNotification(final Context context, final Intent intent)
    {
        final Uri data = intent.getData();
        final Habit habit = Habit.get(ContentUris.parseId(data));
        final Long timestamp = intent.getLongExtra("timestamp", DateUtils.getStartOfToday());
        final Long reminderTime = intent.getLongExtra("reminderTime", DateUtils.getStartOfToday());

        if (habit == null) return;

        new BaseTask()
        {
            int todayValue;

            @Override
            protected void doInBackground()
            {
                todayValue = habit.checkmarks.getTodayValue();
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if (todayValue != Checkmark.UNCHECKED) return;
                if (!checkWeekday(intent, habit)) return;
                if (!habit.hasReminder()) return;

                Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.setData(data);
                PendingIntent contentPendingIntent =
                        PendingIntent.getActivity(context, 0, contentIntent, 0);

                PendingIntent dismissPendingIntent = buildDismissIntent(context);
                PendingIntent checkIntentPending = buildCheckIntent(context, habit, timestamp);
                PendingIntent snoozeIntentPending = buildSnoozeIntent(context, habit);

                Uri ringtoneUri = ReminderUtils.getRingtoneUri(context);

                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender().setBackground(
                                BitmapFactory.decodeResource(context.getResources(),
                                        R.drawable.stripe));

                Notification notification =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(habit.name)
                                .setContentText(habit.description)
                                .setContentIntent(contentPendingIntent)
                                .setDeleteIntent(dismissPendingIntent)
                                .addAction(R.drawable.ic_action_check,
                                        context.getString(R.string.check), checkIntentPending)
                                .addAction(R.drawable.ic_action_snooze,
                                        context.getString(R.string.snooze), snoozeIntentPending)
                                .setSound(ringtoneUri)
                                .extend(wearableExtender)
                                .setWhen(reminderTime)
                                .setShowWhen(true)
                                .build();

                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(
                                Activity.NOTIFICATION_SERVICE);

                int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
                notificationManager.notify(notificationId, notification);

                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    public static PendingIntent buildSnoozeIntent(Context context, Habit habit)
    {
        Uri data = habit.getUri();
        Intent snoozeIntent = new Intent(context, HabitBroadcastReceiver.class);
        snoozeIntent.setData(data);
        snoozeIntent.setAction(ACTION_SNOOZE);
        return PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);
    }

    public static PendingIntent buildCheckIntent(Context context, Habit habit, Long timestamp)
    {
        Uri data = habit.getUri();
        Intent checkIntent = new Intent(context, HabitBroadcastReceiver.class);
        checkIntent.setData(data);
        checkIntent.setAction(ACTION_CHECK);
        if(timestamp != null) checkIntent.putExtra("timestamp", timestamp);
        return PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    public static PendingIntent buildDismissIntent(Context context)
    {
        Intent deleteIntent = new Intent(context, HabitBroadcastReceiver.class);
        deleteIntent.setAction(ACTION_DISMISS);
        return PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
    }

    public static PendingIntent buildViewHabitIntent(Context context, Habit habit)
    {
        Intent intent = new Intent(context, ShowHabitActivity.class);
        intent.setData(Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId()));

        return TaskStackBuilder.create(context.getApplicationContext())
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean checkWeekday(Intent intent, Habit habit)
    {
        Long timestamp = intent.getLongExtra("timestamp", DateUtils.getStartOfToday());

        boolean reminderDays[] = DateUtils.unpackWeekdayList(habit.reminderDays);
        int weekday = DateUtils.getWeekday(timestamp);

        return reminderDays[weekday];
    }

    public static void dismissNotification(Context context, Habit habit)
    {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                        Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }
}
