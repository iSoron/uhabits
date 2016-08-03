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

package org.isoron.uhabits.notifications;

import android.app.*;
import android.content.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.app.NotificationCompat.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

import static android.graphics.BitmapFactory.*;
import static org.isoron.uhabits.utils.RingtoneUtils.*;

public class NotificationTray
{
    @NonNull
    private final Context context;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private final PendingIntentFactory pendingIntents;

    @Inject
    public NotificationTray(@AppContext @NonNull Context context,
                            @NonNull TaskRunner taskRunner,
                            @NonNull PendingIntentFactory pendingIntents)
    {
        this.context = context;
        this.taskRunner = taskRunner;
        this.pendingIntents = pendingIntents;
    }

    public void cancel(@NonNull Habit habit)
    {
        int notificationId = getNotificationId(habit);
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

    public void show(@NonNull Habit habit, long timestamp, long reminderTime)
    {
        taskRunner.execute(
            new ShowNotificationTask(habit, timestamp, reminderTime));
    }

    private int getNotificationId(Habit habit)
    {
        Long id = habit.getId();
        if (id == null) return 0;
        return (int) (id % Integer.MAX_VALUE);
    }

    private class ShowNotificationTask implements Task
    {
        int todayValue;

        private final Habit habit;

        private final long timestamp;

        private final long reminderTime;

        public ShowNotificationTask(Habit habit,
                                    long timestamp,
                                    long reminderTime)
        {
            this.habit = habit;
            this.timestamp = timestamp;
            this.reminderTime = reminderTime;
        }

        @Override
        public void doInBackground()
        {
            todayValue = habit.getCheckmarks().getTodayValue();
        }

        @Override
        public void onPostExecute()
        {
            if (todayValue != Checkmark.UNCHECKED) return;
            if (!shouldShowReminderToday()) return;
            if (!habit.hasReminder()) return;

            WearableExtender wearableExtender =
                new WearableExtender().setBackground(
                    decodeResource(context.getResources(), R.drawable.stripe));

            Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(habit.getName())
                .setContentText(habit.getDescription())
                .setContentIntent(pendingIntents.showHabit(habit))
                .setDeleteIntent(pendingIntents.dismissNotification())
                .addAction(R.drawable.ic_action_check,
                    context.getString(R.string.check),
                    pendingIntents.addCheckmark(habit, timestamp))
                .addAction(R.drawable.ic_action_snooze,
                    context.getString(R.string.snooze),
                    pendingIntents.snoozeNotification(habit))
                .setSound(getRingtoneUri(context))
                .extend(wearableExtender)
                .setWhen(reminderTime)
                .setShowWhen(true)
                .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                    Activity.NOTIFICATION_SERVICE);

            int notificationId = getNotificationId(habit);
            notificationManager.notify(notificationId, notification);
        }

        private boolean shouldShowReminderToday()
        {
            if (!habit.hasReminder()) return false;
            Reminder reminder = habit.getReminder();

            boolean reminderDays[] = reminder.getDays().toArray();
            int weekday = DateUtils.getWeekday(timestamp);

            return reminderDays[weekday];
        }

    }
}
