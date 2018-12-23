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

package org.isoron.uhabits.core.ui;

import android.support.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;

import java.util.*;

import javax.inject.*;


@AppScope
public class NotificationTray
    implements CommandRunner.Listener, Preferences.Listener
{
    public static final String REMINDERS_CHANNEL_ID = "REMINDERS";

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final Preferences preferences;

    private SystemTray systemTray;

    @NonNull
    private final HashMap<Habit, NotificationData> active;

    @Inject
    public NotificationTray(@NonNull TaskRunner taskRunner,
                            @NonNull CommandRunner commandRunner,
                            @NonNull Preferences preferences,
                            @NonNull SystemTray systemTray)
    {
        this.taskRunner = taskRunner;
        this.commandRunner = commandRunner;
        this.preferences = preferences;
        this.systemTray = systemTray;
        this.active = new HashMap<>();
    }

    public void cancel(@NonNull Habit habit)
    {
        int notificationId = getNotificationId(habit);
        systemTray.removeNotification(notificationId);
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
            taskRunner.execute(() ->
            {
                if (habit.getCheckmarks().getTodayValue() !=
                    Checkmark.UNCHECKED) cancel(habit);
            });
        }

        if (command instanceof DeleteHabitsCommand)
        {
            DeleteHabitsCommand deleteCommand = (DeleteHabitsCommand) command;
            List<Habit> deleted = deleteCommand.getSelected();
            for (Habit habit : deleted)
                cancel(habit);
        }
    }

    @Override
    public void onNotificationsChanged()
    {
        reshowAll();
    }

    public void show(@NonNull Habit habit, Timestamp timestamp, long reminderTime)
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

    public interface SystemTray
    {
        void removeNotification(int notificationId);

        void showNotification(Habit habit,
                              int notificationId,
                              Timestamp timestamp,
                              long reminderTime);
    }

    class NotificationData
    {
        public final Timestamp timestamp;

        public final long reminderTime;

        public NotificationData(Timestamp timestamp, long reminderTime)
        {
            this.timestamp = timestamp;
            this.reminderTime = reminderTime;
        }
    }

    private class ShowNotificationTask implements Task
    {
        int todayValue;

        private final Habit habit;

        private final Timestamp timestamp;

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

            systemTray.showNotification(habit, getNotificationId(habit), timestamp,
                reminderTime);
        }

        private boolean shouldShowReminderToday()
        {
            if (!habit.hasReminder()) return false;
            Reminder reminder = habit.getReminder();

            boolean reminderDays[] = reminder.getDays().toArray();
            int weekday = timestamp.getWeekday();

            return reminderDays[weekday];
        }
    }
}
