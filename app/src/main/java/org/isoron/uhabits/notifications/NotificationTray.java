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
import android.graphics.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.app.NotificationCompat.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import javax.inject.*;

import static android.graphics.BitmapFactory.*;
import static org.isoron.uhabits.utils.RingtoneUtils.*;

@AppScope
public class NotificationTray
    implements CommandRunner.Listener, Preferences.Listener
{
    @NonNull
    private final Context context;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private final PendingIntentFactory pendingIntents;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final Preferences preferences;

    @NonNull
    private final HashMap<Habit, NotificationData> active;

    @Inject
    public NotificationTray(@AppContext @NonNull Context context,
                            @NonNull TaskRunner taskRunner,
                            @NonNull PendingIntentFactory pendingIntents,
                            @NonNull CommandRunner commandRunner,
                            @NonNull Preferences preferences)
    {
        this.context = context;
        this.taskRunner = taskRunner;
        this.pendingIntents = pendingIntents;
        this.commandRunner = commandRunner;
        this.preferences = preferences;
        this.active = new HashMap<>();
    }

    public void cancel(@NonNull Habit habit)
    {
        int notificationId = getNotificationId(habit);
        NotificationManagerCompat.from(context).cancel(notificationId);
        active.remove(habit);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if (command instanceof ToggleRepetitionCommand)
        {
            ToggleRepetitionCommand toggleCmd =
                (ToggleRepetitionCommand) command;

            Habit habit = toggleCmd.getHabit();
            if (habit.getCheckmarks().getTodayValue() != Checkmark.UNCHECKED)
                cancel(habit);
        }

        if (command instanceof DeleteHabitsCommand)
        {
            DeleteHabitsCommand deleteCommand = (DeleteHabitsCommand) command;
            List<Habit> deleted = deleteCommand.getHabits();
            for (Habit habit : deleted)
                cancel(habit);
        }
    }

    @Override
    public void onNotificationsChanged()
    {
        reshowAll();
    }

    public void show(@NonNull Habit habit, long timestamp, long reminderTime)
    {
        NotificationData data = new NotificationData(timestamp, reminderTime);
        active.put(habit, data);
        taskRunner.execute(new ShowNotificationTask(habit, data));
    }

    public void startListening()
    {
        commandRunner.addListener(this);
        preferences.addListener(this);
    }

    public void stopListening()
    {
        commandRunner.removeListener(this);
        preferences.removeListener(this);
    }

    private int getNotificationId(Habit habit)
    {
        Long id = habit.getId();
        if (id == null) return 0;
        return (int) (id % Integer.MAX_VALUE);
    }

    private void reshowAll()
    {
        for (Habit habit : active.keySet())
        {
            NotificationData data = active.get(habit);
            taskRunner.execute(new ShowNotificationTask(habit, data));
        }
    }

    class NotificationData
    {
        public final long timestamp;

        public final long reminderTime;

        public NotificationData(long timestamp, long reminderTime)
        {
            this.timestamp = timestamp;
            this.reminderTime = reminderTime;
        }
    }

    private class ShowNotificationTask implements Task
    {
        int todayValue;

        private final Habit habit;

        private final long timestamp;

        private final long reminderTime;

        public ShowNotificationTask(Habit habit, NotificationData data)
        {
            this.habit = habit;
            this.timestamp = data.timestamp;
            this.reminderTime = data.reminderTime;
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

            Action checkAction = new Action(R.drawable.ic_action_check,
                context.getString(R.string.check),
                pendingIntents.addCheckmark(habit, timestamp));

            Action snoozeAction = new Action(R.drawable.ic_action_snooze,
                context.getString(R.string.snooze),
                pendingIntents.snoozeNotification(habit));

            Bitmap wearableBg =
                decodeResource(context.getResources(), R.drawable.stripe);

            // Even though the set of actions is the same on the phone and
            // on the watch, Pebble requires us to add them to the
            // WearableExtender.
            WearableExtender wearableExtender = new WearableExtender()
                .setBackground(wearableBg)
                .addAction(checkAction)
                .addAction(snoozeAction);

            Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(habit.getName())
                .setContentText(habit.getDescription())
                .setContentIntent(pendingIntents.showHabit(habit))
                .setDeleteIntent(pendingIntents.dismissNotification(habit))
                .addAction(checkAction)
                .addAction(snoozeAction)
                .setSound(getRingtoneUri(context))
                .extend(wearableExtender)
                .setWhen(reminderTime)
                .setShowWhen(true)
                .setOngoing(preferences.shouldMakeNotificationsSticky())
                .build();

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
