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

package org.isoron.uhabits.io;

import android.database.*;
import android.database.sqlite.*;
import android.support.annotation.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.DatabaseUtils;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

import static android.database.sqlite.SQLiteDatabase.*;

/**
 * Class that imports database files exported by Rewire.
 */
public class RewireDBImporter extends AbstractImporter
{
    private ModelFactory modelFactory;

    @Inject
    public RewireDBImporter(@NonNull HabitList habits,
                            @NonNull ModelFactory modelFactory)
    {
        super(habits);
        this.modelFactory = modelFactory;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        SQLiteDatabase db = openDatabase(file.getPath(), null, OPEN_READONLY);

        Cursor c = db.rawQuery(
            "select count(*) from SQLITE_MASTER where name=? or name=?",
            new String[]{ "CHECKINS", "UNIT" });

        boolean result = (c.moveToFirst() && c.getInt(0) == 2);

        c.close();
        db.close();
        return result;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
        String path = file.getPath();
        final SQLiteDatabase db = openDatabase(path, null, OPEN_READONLY);

        DatabaseUtils.executeAsTransaction(() -> createHabits(db));
        db.close();
    }

    private void createCheckmarks(@NonNull SQLiteDatabase db,
                                  @NonNull Habit habit,
                                  int rewireHabitId)
    {
        Cursor c = null;

        try
        {
            String[] params = { Integer.toString(rewireHabitId) };
            c = db.rawQuery(
                "select distinct date from checkins where habit_id=? and type=2",
                params);
            if (!c.moveToFirst()) return;

            do
            {
                String date = c.getString(0);
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));

                GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
                cal.set(year, month - 1, day);

                habit.getRepetitions().toggleTimestamp(cal.getTimeInMillis());
            } while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }

    private void createHabits(SQLiteDatabase db)
    {
        Cursor c = null;

        try
        {
            c = db.rawQuery(
                "select _id, name, description, schedule, active_days, " +
                "repeating_count, days, period from habits", new String[0]);
            if (!c.moveToFirst()) return;

            do
            {
                int id = c.getInt(0);
                String name = c.getString(1);
                String description = c.getString(2);
                int schedule = c.getInt(3);
                String activeDays = c.getString(4);
                int repeatingCount = c.getInt(5);
                int days = c.getInt(6);
                int periodIndex = c.getInt(7);

                Habit habit = modelFactory.buildHabit();
                habit.setName(name);
                habit.setDescription(description);

                int periods[] = { 7, 31, 365 };
                int numerator, denominator;

                switch (schedule)
                {
                    case 0:
                        numerator = activeDays.split(",").length;
                        denominator = 7;
                        break;

                    case 1:
                        numerator = days;
                        denominator = (periods[periodIndex]);
                        break;

                    case 2:
                        numerator = 1;
                        denominator = repeatingCount;
                        break;

                    default:
                        throw new IllegalStateException();
                }

                habit.setFrequency(new Frequency(numerator, denominator));
                habits.add(habit);

                createReminder(db, habit, id);
                createCheckmarks(db, habit, id);

            } while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }

    private void createReminder(SQLiteDatabase db,
                                Habit habit,
                                int rewireHabitId)
    {
        String[] params = { Integer.toString(rewireHabitId) };
        Cursor c = null;

        try
        {
            c = db.rawQuery(
                "select time, active_days from reminders where habit_id=? limit 1",
                params);

            if (!c.moveToFirst()) return;
            int rewireReminder = Integer.parseInt(c.getString(0));
            if (rewireReminder <= 0 || rewireReminder >= 1440) return;

            boolean reminderDays[] = new boolean[7];

            String activeDays[] = c.getString(1).split(",");
            for (String d : activeDays)
            {
                int idx = (Integer.parseInt(d) + 1) % 7;
                reminderDays[idx] = true;
            }

            int hour = rewireReminder / 60;
            int minute = rewireReminder % 60;
            WeekdayList days = new WeekdayList(reminderDays);

            Reminder reminder = new Reminder(hour, minute, days);
            habit.setReminder(reminder);
            habits.update(habit);
        }
        finally
        {
            if (c != null) c.close();
        }
    }
}
