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

import android.support.annotation.*;
import android.util.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

import javax.inject.*;

@AppScope
public class HabitLogger
{
    @Inject
    public HabitLogger()
    {

    }

    public void logReminderScheduled(@NonNull Habit habit,
                                     @NonNull Long reminderTime)
    {
        int min = Math.min(3, habit.getName().length());
        String name = habit.getName().substring(0, min);

        DateFormat df = DateFormats.getBackupDateFormat();
        String time = df.format(new Date(reminderTime));

        Log.i("ReminderHelper",
                String.format("Setting alarm (%s): %s", time, name));
    }
}
