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

import org.isoron.uhabits.*;

import javax.inject.*;

import static android.app.AlarmManager.*;
import static android.content.Context.*;

@AppScope
public class IntentScheduler
{
    private final AlarmManager manager;

    @Inject
    public IntentScheduler(@AppContext Context context)
    {
        manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    public void schedule(@NonNull Long timestamp, PendingIntent intent)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, timestamp, intent);
            return;
        }

        if (Build.VERSION.SDK_INT >= 19)
        {
            manager.setExact(RTC_WAKEUP, timestamp, intent);
            return;
        }

        manager.set(RTC_WAKEUP, timestamp, intent);
    }
}
