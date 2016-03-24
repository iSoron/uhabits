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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

public class RewireDBImporter extends AbstractImporter
{
    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if(!isSQLite3File(file)) return false;

        SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null,
                SQLiteDatabase.OPEN_READONLY);

        Cursor c = db.rawQuery("select count(*) from SQLITE_MASTER where name=? or name=?",
                new String[]{"CHECKINS", "UNIT"});

        boolean result = (c.moveToFirst() && c.getInt(0) == 2);

        c.close();
        db.close();
        return result;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null,
                SQLiteDatabase.OPEN_READONLY);

        DatabaseHelper.executeAsTransaction(new DatabaseHelper.Command()
        {
            @Override
            public void execute()
            {
                createHabits(db);
            }
        });

        db.close();
    }

    private void createHabits(SQLiteDatabase db)
    {
        Cursor c = null;

        try
        {
            c = db.rawQuery("select _id, name, description, schedule, active_days, " +
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

                Habit habit = new Habit();
                habit.name = name;
                habit.description = description;

                int periods[] = { 7, 31, 365 };

                switch (schedule)
                {
                    case 0:
                        habit.freqNum = activeDays.split(",").length;
                        habit.freqDen = 7;
                        break;

                    case 1:
                        habit.freqNum = days;
                        habit.freqDen = periods[periodIndex];
                        break;

                    case 2:
                        habit.freqNum = 1;
                        habit.freqDen = repeatingCount;
                        break;
                }

                habit.save();

                createReminder(db, habit, id);
                createCheckmarks(db, habit, id);

            }
            while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }

    private void createReminder(SQLiteDatabase db, Habit habit, int rewireHabitId)
    {
        String[] params = { Integer.toString(rewireHabitId) };
        Cursor c = null;

        try
        {
            c = db.rawQuery("select time, active_days from reminders where habit_id=? limit 1", params);

            if (!c.moveToFirst()) return;
            int rewireReminder = Integer.parseInt(c.getString(0));
            if (rewireReminder <= 0 || rewireReminder >= 1440) return;

            boolean reminderDays[] = new boolean[7];

            String activeDays[] = c.getString(1).split(",");
            for(String d : activeDays)
            {
                int idx = (Integer.parseInt(d) + 1) % 7;
                reminderDays[idx] = true;
            }

            habit.reminderDays = DateHelper.packWeekdayList(reminderDays);
            habit.reminderHour = rewireReminder / 60;
            habit.reminderMin = rewireReminder % 60;
            habit.save();
        }
        finally
        {
            if(c != null) c.close();
        }
    }

    private void createCheckmarks(@NonNull SQLiteDatabase db, @NonNull Habit habit, int rewireHabitId)
    {
        Cursor c = null;

        try
        {
            String[] params = { Integer.toString(rewireHabitId) };
            c = db.rawQuery("select distinct date from checkins where habit_id=? and type=2", params);
            if (!c.moveToFirst()) return;

            do
            {
                String date = c.getString(0);
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));

                GregorianCalendar cal = DateHelper.getStartOfTodayCalendar();
                cal.set(year, month - 1, day);

                habit.repetitions.toggle(cal.getTimeInMillis());
            }
            while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }
}
