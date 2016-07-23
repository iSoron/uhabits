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

package org.isoron.uhabits.receivers;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 * <p>
 * All broadcast messages are received and processed by this class.
 */
public class ReminderReceiver extends BroadcastReceiver
{
    public static final String ACTION_DISMISS_REMINDER =
        "org.isoron.uhabits.ACTION_DISMISS_REMINDER";

    public static final String ACTION_SHOW_REMINDER =
        "org.isoron.uhabits.ACTION_SHOW_REMINDER";

    public static final String ACTION_SNOOZE_REMINDER =
        "org.isoron.uhabits.ACTION_SNOOZE_REMINDER";

    private static final String TAG = "ReminderReceiver";

    private final HabitList habits;

    private final TaskRunner taskRunner;

    private final ReminderScheduler reminderScheduler;

    public ReminderReceiver()
    {
        super();

        BaseComponent component = HabitsApplication.getComponent();
        habits = component.getHabitList();
        taskRunner = component.getTaskRunner();
        reminderScheduler = component.getReminderScheduler();
    }

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        Log.i(TAG, String.format("Received intent: %s", intent.toString()));

        try
        {
            switch (intent.getAction())
            {
                case ACTION_SHOW_REMINDER:
                    onActionShowReminder(context, intent);
                    break;

                case ACTION_DISMISS_REMINDER:
                    // NOP
                    break;

                case ACTION_SNOOZE_REMINDER:
                    onActionSnoozeReminder(context, intent);
                    break;

                case Intent.ACTION_BOOT_COMPLETED:
                    onActionBootCompleted();
                    break;
            }
        }
        catch (RuntimeException e)
        {
            Log.e(TAG, "could not process intent", e);
        }
    }

    protected void onActionBootCompleted()
    {
        reminderScheduler.schedule(habits);
    }

    protected void onActionShowReminder(Context context, Intent intent)
    {
        createNotification(context, intent);
        createReminderAlarmsDelayed();
    }

    private void createNotification(final Context context, final Intent intent)
    {
        final Uri data = intent.getData();
        final Habit habit = habits.getById(ContentUris.parseId(data));
        final Long timestamp =
            intent.getLongExtra("timestamp", DateUtils.getStartOfToday());
        final Long reminderTime =
            intent.getLongExtra("reminderTime", DateUtils.getStartOfToday());

        if (habit == null) return;

        taskRunner.execute(new Task()
        {
            int todayValue;

            @Override
            public void doInBackground()
            {
                todayValue = habit.getCheckmarks().getTodayValue();
            }

            @Override
            public void onPostExecute()
            {
                if (todayValue != Checkmark.UNCHECKED) return;
                if (!shouldShowReminderToday(intent, habit)) return;
                if (!habit.hasReminder()) return;

                Intent contentIntent = new Intent(context, MainActivity.class);
                contentIntent.setData(data);
                PendingIntent contentPendingIntent =
                    PendingIntent.getActivity(context, 0, contentIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                PendingIntentFactory pendingIntentFactory =
                    new PendingIntentFactory(context);

                PendingIntent dismissPendingIntent;
                dismissPendingIntent =
                    pendingIntentFactory.dismissNotification();
                PendingIntent checkIntentPending =
                    pendingIntentFactory.addCheckmark(habit, timestamp);
                PendingIntent snoozeIntentPending =
                    pendingIntentFactory.snoozeNotification(habit);

                Uri ringtoneUri = RingtoneUtils.getRingtoneUri(context);

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
            }
        });
    }

    private void createReminderAlarmsDelayed()
    {
        new Handler().postDelayed(() -> {
            reminderScheduler.schedule(habits);
        }, 5000);
    }

    private void dismissNotification(Context context, Long habitId)
    {
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habitId % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }

    private void onActionSnoozeReminder(Context context, Intent intent)
    {
        Uri data = intent.getData();
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        long delayMinutes =
            Long.parseLong(prefs.getString("pref_snooze_interval", "15"));

        long habitId = ContentUris.parseId(data);
        Habit habit = habits.getById(habitId);

        if (habit != null)
        {
            long reminderTime = new Date().getTime() + delayMinutes * 60 * 1000;
            reminderScheduler.schedule(habit, reminderTime);
        }

        dismissNotification(context, habitId);
    }

    private boolean shouldShowReminderToday(Intent intent, Habit habit)
    {
        if (!habit.hasReminder()) return false;
        Reminder reminder = habit.getReminder();

        Long timestamp =
            intent.getLongExtra("timestamp", DateUtils.getStartOfToday());

        boolean reminderDays[] = reminder.getDays().toArray();
        int weekday = DateUtils.getWeekday(timestamp);

        return reminderDays[weekday];
    }
}
