/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.*;
import android.net.*;
import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.reminders.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.notifications.*;

import javax.inject.*;

import static org.isoron.uhabits.core.utils.DateUtils.applyTimezone;
import static org.isoron.uhabits.core.utils.DateUtils.getLocalTime;

@ReceiverScope
public class ReminderController
{
    @NonNull
    private final ReminderScheduler reminderScheduler;

    @NonNull
    private final NotificationTray notificationTray;

    private Preferences preferences;

    @Inject
    public ReminderController(@NonNull ReminderScheduler reminderScheduler,
                              @NonNull NotificationTray notificationTray,
                              @NonNull Preferences preferences)
    {
        this.reminderScheduler = reminderScheduler;
        this.notificationTray = notificationTray;
        this.preferences = preferences;
    }

    public void onBootCompleted()
    {
        reminderScheduler.scheduleAll();
    }

    public void onShowReminder(@NonNull Habit habit,
                               Timestamp timestamp,
                               long reminderTime)
    {
        notificationTray.show(habit, timestamp, reminderTime);
        reminderScheduler.scheduleHabitAtReminder(habit);
    }

    public void onSnooze(@NonNull Habit habit, final Context context)
    {
        long snoozeInterval = preferences.getSnoozeInterval();

        if (snoozeInterval < 0)
        {
            context.sendBroadcast( new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            Intent intent = new Intent( SnoozeDelayActivity.ACTION_ASK_SNOOZE, Uri.parse( habit.getUriString()),
                    context, SnoozeDelayActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        snoozeNotificationAddDelay(habit, snoozeInterval);
    }

    public void snoozeNotificationAddDelay(@NonNull Habit habit, long snoozeInterval)
    {
        long now = applyTimezone(getLocalTime());
        long reminderTime = now + snoozeInterval * 60 * 1000;
        snoozeNotificationSetReminderTime(habit, reminderTime);
    }

    public void snoozeNotificationSetReminderTime(@NonNull Habit habit, long reminderTime)
    {
        reminderScheduler.scheduleHabitAtCustom(habit, reminderTime);
        notificationTray.cancel(habit);
    }

    public void onDismiss(@NonNull Habit habit)
    {
        notificationTray.cancel(habit);
    }
}
