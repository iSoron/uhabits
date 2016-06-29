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
import android.net.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.show.*;

public abstract class HabitPendingIntents
{

    private static final String BASE_URL =
        "content://org.isoron.uhabits/habit/";

    public static PendingIntent dismissNotification(Context context)
    {
        Intent deleteIntent = new Intent(context, HabitBroadcastReceiver.class);
        deleteIntent.setAction(HabitBroadcastReceiver.ACTION_DISMISS);
        return PendingIntent.getBroadcast(context, 0, deleteIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent snoozeNotification(Context context, Habit habit)
    {
        Uri data = habit.getUri();
        Intent snoozeIntent = new Intent(context, HabitBroadcastReceiver.class);
        snoozeIntent.setData(data);
        snoozeIntent.setAction(HabitBroadcastReceiver.ACTION_SNOOZE);
        return PendingIntent.getBroadcast(context, 0, snoozeIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent toggleCheckmark(Context context,
                                                Habit habit,
                                                Long timestamp)
    {
        Uri data = habit.getUri();
        Intent checkIntent = new Intent(context, HabitBroadcastReceiver.class);
        checkIntent.setData(data);
        checkIntent.setAction(HabitBroadcastReceiver.ACTION_CHECK);
        if (timestamp != null) checkIntent.putExtra("timestamp", timestamp);
        return PendingIntent.getBroadcast(context, 0, checkIntent,
            PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent viewHabit(Context context, Habit habit)
    {
        Intent intent = new Intent(context, ShowHabitActivity.class);
        intent.setData(Uri.parse(BASE_URL + habit.getId()));
        return android.support.v4.app.TaskStackBuilder
            .create(context.getApplicationContext())
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
