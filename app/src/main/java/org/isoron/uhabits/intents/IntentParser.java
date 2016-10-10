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

import android.content.*;
import android.net.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

import static android.content.ContentUris.*;

@AppScope
public class IntentParser
{
    private HabitList habits;

    @Inject
    public IntentParser(@NonNull HabitList habits)
    {
        this.habits = habits;
    }

    public CheckmarkIntentData parseCheckmarkIntent(@NonNull Intent intent)
    {
        Uri uri = intent.getData();
        if (uri == null) throw new IllegalArgumentException("uri is null");

        CheckmarkIntentData data = new CheckmarkIntentData();
        data.habit = parseHabit(uri);
        data.timestamp = parseTimestamp(intent);
        return data;
    }

    @NonNull
    protected Habit parseHabit(@NonNull Uri uri)
    {
        Habit habit = habits.getById(parseId(uri));
        if (habit == null)
            throw new IllegalArgumentException("habit not found");
        return habit;
    }

    @NonNull
    protected Long parseTimestamp(@NonNull Intent intent)
    {
        long today = DateUtils.getStartOfToday();
        Long timestamp = intent.getLongExtra("timestamp", today);
        timestamp = DateUtils.getStartOfDay(timestamp);

        if (timestamp < 0 || timestamp > today)
            throw new IllegalArgumentException("timestamp is not valid");

        return timestamp;
    }

    public class CheckmarkIntentData
    {
        public Habit habit;

        public Long timestamp;
    }
}
