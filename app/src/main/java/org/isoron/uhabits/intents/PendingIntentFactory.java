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

package org.isoron.uhabits.intents;

import android.app.*;
import android.content.*;
import android.net.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.receivers.*;

import javax.inject.*;

import static android.app.PendingIntent.*;

@AppScope
public class PendingIntentFactory
{
    private final Context context;

    private IntentFactory intentFactory;

    @Inject
    public PendingIntentFactory(@AppContext Context context,
                                @NonNull IntentFactory intentFactory)
    {
        this.context = context;
        this.intentFactory = intentFactory;
    }

    public PendingIntent addCheckmark(@NonNull Habit habit,
                                      @Nullable Long timestamp)
    {
        Intent checkIntent = new Intent(context, WidgetReceiver.class);
        checkIntent.setData(habit.getUri());
        checkIntent.setAction(WidgetReceiver.ACTION_ADD_REPETITION);
        if (timestamp != null) checkIntent.putExtra("timestamp", timestamp);
        return PendingIntent.getBroadcast(context, 1, checkIntent,
            FLAG_UPDATE_CURRENT);
    }

    public PendingIntent dismissNotification(@NonNull Habit habit)
    {
        Intent deleteIntent = new Intent(context, ReminderReceiver.class);
        deleteIntent.setAction(WidgetReceiver.ACTION_DISMISS_REMINDER);
        deleteIntent.setData(habit.getUri());
        return PendingIntent.getBroadcast(context, 0, deleteIntent,
            FLAG_UPDATE_CURRENT);
    }

    public PendingIntent showHabit(Habit habit)
    {
        Intent intent = intentFactory.startShowHabitActivity(context, habit);

        return android.support.v4.app.TaskStackBuilder
            .create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(0, FLAG_UPDATE_CURRENT);
    }

    public PendingIntent showReminder(@NonNull Habit habit,
                                      @Nullable Long reminderTime,
                                      long timestamp)
    {
        Uri uri = habit.getUri();

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_SHOW_REMINDER);
        intent.setData(uri);
        intent.putExtra("timestamp", timestamp);
        intent.putExtra("reminderTime", reminderTime);
        int reqCode = ((int) (habit.getId() % Integer.MAX_VALUE)) + 1;
        return PendingIntent.getBroadcast(context, reqCode, intent,
            FLAG_UPDATE_CURRENT);
    }

    public PendingIntent snoozeNotification(@NonNull Habit habit)
    {
        Uri data = habit.getUri();
        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setData(data);
        snoozeIntent.setAction(ReminderReceiver.ACTION_SNOOZE_REMINDER);
        return PendingIntent.getBroadcast(context, 0, snoozeIntent,
            FLAG_UPDATE_CURRENT);
    }

    public PendingIntent toggleCheckmark(@NonNull Habit habit,
                                         @Nullable Long timestamp)
    {
        Uri data = habit.getUri();
        Intent checkIntent = new Intent(context, WidgetReceiver.class);
        checkIntent.setData(data);
        checkIntent.setAction(WidgetReceiver.ACTION_TOGGLE_REPETITION);
        if (timestamp != null) checkIntent.putExtra("timestamp", timestamp);
        return PendingIntent.getBroadcast(context, 2, checkIntent,
            FLAG_UPDATE_CURRENT);
    }
}
