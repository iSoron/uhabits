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

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import javax.inject.*;

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 * <p/>
 * All broadcast messages are received and processed by this class.
 */
public class HabitBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_CHECK = "org.isoron.uhabits.ACTION_CHECK";

    public static final String ACTION_DISMISS =
        "org.isoron.uhabits.ACTION_DISMISS";

    public static final String ACTION_SHOW_REMINDER =
        "org.isoron.uhabits.ACTION_SHOW_REMINDER";

    public static final String ACTION_SNOOZE =
        "org.isoron.uhabits.ACTION_SNOOZE";

    @Inject
    HabitList habitList;

    @Inject
    CommandRunner commandRunner;

    public HabitBroadcastReceiver()
    {
        super();
        HabitsApplication.getComponent().inject(this);
    }

    public static void dismissNotification(Context context, Habit habit)
    {
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }

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
                ReminderUtils.createReminderAlarms(context, habitList);
                break;
        }
    }

    private void checkHabit(Context context, Intent intent)
    {
        Uri data = intent.getData();
        long today = DateUtils.getStartOfToday();
        Long timestamp = intent.getLongExtra("timestamp", today);

        long habitId = ContentUris.parseId(data);
        Habit habit = habitList.getById(habitId);
        if (habit != null)
        {
            ToggleRepetitionCommand command =
                new ToggleRepetitionCommand(habit, timestamp);
            commandRunner.execute(command, habitId);
        }

        dismissNotification(context, habitId);
    }

    private boolean checkWeekday(Intent intent, Habit habit)
    {
        if (!habit.hasReminder()) return false;
        Reminder reminder = habit.getReminder();

        Long timestamp =
            intent.getLongExtra("timestamp", DateUtils.getStartOfToday());

        boolean reminderDays[] =
            DateUtils.unpackWeekdayList(reminder.getDays());
        int weekday = DateUtils.getWeekday(timestamp);

        return reminderDays[weekday];
    }

    private void createNotification(final Context context, final Intent intent)
    {
        final Uri data = intent.getData();
        final Habit habit = habitList.getById(ContentUris.parseId(data));
        final Long timestamp =
            intent.getLongExtra("timestamp", DateUtils.getStartOfToday());
        final Long reminderTime =
            intent.getLongExtra("reminderTime", DateUtils.getStartOfToday());

        if (habit == null) return;

        new BaseTask()
        {
            int todayValue;

            @Override
            protected void doInBackground()
            {
                todayValue = habit.getCheckmarks().getTodayValue();
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
                    PendingIntent.getActivity(context, 0, contentIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                PendingIntent dismissPendingIntent;
                dismissPendingIntent =
                    HabitPendingIntents.dismissNotification(context);
                PendingIntent checkIntentPending =
                    HabitPendingIntents.toggleCheckmark(context, habit, timestamp);
                PendingIntent snoozeIntentPending =
                    HabitPendingIntents.snoozeNotification(context, habit);

                Uri ringtoneUri = ReminderUtils.getRingtoneUri(context);

                NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender().setBackground(
                        BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.stripe));

                Notification notification =
                    new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(habit.getName())
                        .setContentText(habit.getDescription())
                        .setContentIntent(contentPendingIntent)
                        .setDeleteIntent(dismissPendingIntent)
                        .addAction(R.drawable.ic_action_check,
                            context.getString(R.string.check),
                            checkIntentPending)
                        .addAction(R.drawable.ic_action_snooze,
                            context.getString(R.string.snooze),
                            snoozeIntentPending)
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

    private void createReminderAlarmsDelayed(final Context context)
    {
        new Handler().postDelayed(
            () -> ReminderUtils.createReminderAlarms(context, habitList), 5000);
    }

    private void dismissAllHabits()
    {

    }

    private void dismissNotification(Context context, Long habitId)
    {
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habitId % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }

    private void snoozeHabit(Context context, Intent intent)
    {
        Uri data = intent.getData();
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        long delayMinutes =
            Long.parseLong(prefs.getString("pref_snooze_interval", "15"));

        long habitId = ContentUris.parseId(data);
        Habit habit = habitList.getById(habitId);
        if (habit != null) ReminderUtils.createReminderAlarm(context, habit,
            new Date().getTime() + delayMinutes * 60 * 1000);
        dismissNotification(context, habitId);
    }
}
