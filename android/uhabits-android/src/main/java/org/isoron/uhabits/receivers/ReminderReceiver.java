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

import android.content.*;
import android.util.*;

import androidx.annotation.Nullable;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;

import static android.content.ContentUris.*;

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

    @Override
    public void onReceive(@Nullable final Context context, @Nullable Intent intent)
    {
        if (context == null || intent == null) return;
        if (intent.getAction() == null) return;

        HabitsApplication app = (HabitsApplication) context.getApplicationContext();
        HabitsApplicationComponent appComponent = app.getComponent();
        HabitList habits = appComponent.getHabitList();
        ReminderController reminderController = appComponent.getReminderController();

        Log.i(TAG, String.format("Received intent: %s", intent.toString()));

        Habit habit = null;
        long today = DateUtils.getStartOfTodayWithOffset();

        if (intent.getData() != null)
            habit = habits.getById(parseId(intent.getData()));
        final long timestamp = intent.getLongExtra("timestamp", today);
        final long reminderTime = intent.getLongExtra("reminderTime", today);

        try
        {
            switch (intent.getAction())
            {
                case ACTION_SHOW_REMINDER:
                    if (habit == null) return;
                    Log.d("ReminderReceiver", String.format(
                            "onShowReminder habit=%d timestamp=%d reminderTime=%d",
                            habit.id,
                            timestamp,
                            reminderTime));
                    reminderController.onShowReminder(habit,
                            new Timestamp(timestamp), reminderTime);
                    break;

                case ACTION_DISMISS_REMINDER:
                    if (habit == null) return;
                    Log.d("ReminderReceiver", String.format("onDismiss habit=%d", habit.id));
                    reminderController.onDismiss(habit);
                    break;

                case ACTION_SNOOZE_REMINDER:
                    if (habit == null) return;
                    Log.d("ReminderReceiver", String.format("onSnoozePressed habit=%d", habit.id));
                    reminderController.onSnoozePressed(habit, context);
                    break;

                case Intent.ACTION_BOOT_COMPLETED:
                    Log.d("ReminderReceiver", "onBootCompleted");
                    reminderController.onBootCompleted();
                    break;
            }
        }
        catch (RuntimeException e)
        {
            Log.e(TAG, "could not process intent", e);
        }
    }
}
