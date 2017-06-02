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
import android.os.*;
import android.support.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;

import javax.inject.*;

import static android.app.AlarmManager.*;
import static android.content.Context.*;
import static android.os.Build.VERSION_CODES.*;

@AppScope
public class IntentScheduler
    implements org.isoron.uhabits.core.reminders.ReminderScheduler.SystemScheduler
{
    private final AlarmManager manager;

    @NonNull
    private final PendingIntentFactory pendingIntents;

    private HabitLogger logger;

    @Inject
    public IntentScheduler(@AppContext Context context,
                           @NonNull PendingIntentFactory pendingIntents,
                           @NonNull HabitLogger logger)
    {
        manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        this.pendingIntents = pendingIntents;
        this.logger = logger;
    }

    public void schedule(@NonNull Long timestamp, PendingIntent intent)
    {
        if (Build.VERSION.SDK_INT >= M)
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, timestamp, intent);
        else manager.setExact(RTC_WAKEUP, timestamp, intent);
    }

    @Override
    public void scheduleShowReminder(long reminderTime,
                                     @NonNull Habit habit,
                                     long timestamp)
    {
        schedule(reminderTime,
            pendingIntents.showReminder(habit, reminderTime, timestamp));
        logger.logReminderScheduled(habit, reminderTime);
    }
}
