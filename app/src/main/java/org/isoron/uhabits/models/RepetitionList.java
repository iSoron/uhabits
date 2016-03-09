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

package org.isoron.uhabits.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.isoron.helpers.DateHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class RepetitionList
{

    private Habit habit;

    public RepetitionList(Habit habit)
    {
        this.habit = habit;
    }

    protected From select()
    {
        return new Select().from(Repetition.class)
                .where("habit = ?", habit.getId())
                .orderBy("timestamp");
    }

    protected From selectFromTo(long timeFrom, long timeTo)
    {
        return select().and("timestamp >= ?", timeFrom).and("timestamp <= ?", timeTo);
    }

    public boolean contains(long timestamp)
    {
        int count = select().where("timestamp = ?", timestamp).count();
        return (count > 0);
    }

    public void delete(long timestamp)
    {
        new Delete().from(Repetition.class)
                .where("habit = ?", habit.getId())
                .and("timestamp = ?", timestamp)
                .execute();
    }

    public Repetition getOldestNewerThan(long timestamp)
    {
        return select().where("timestamp > ?", timestamp).limit(1).executeSingle();
    }

    public void toggle(long timestamp)
    {
        timestamp = DateHelper.getStartOfDay(timestamp);

        if (contains(timestamp))
        {
            delete(timestamp);
        }
        else
        {
            Repetition rep = new Repetition();
            rep.habit = habit;
            rep.timestamp = timestamp;
            rep.save();
        }

        habit.scores.deleteNewerThan(timestamp);
        habit.checkmarks.deleteNewerThan(timestamp);
        habit.streaks.deleteNewerThan(timestamp);
    }

    public Repetition getOldest()
    {
        return (Repetition) select().limit(1).executeSingle();
    }

    public boolean hasImplicitRepToday()
    {
        long today = DateHelper.getStartOfToday();
        int reps[] = habit.checkmarks.getValues(today - DateHelper.millisecondsInOneDay, today);
        return (reps[0] > 0);
    }

    public HashMap<Long, Integer[]> getWeekdayFrequency()
    {
        Repetition oldestRep = getOldest();
        if(oldestRep == null) return new HashMap<>();

        String query = "select strftime('%Y', timestamp / 1000, 'unixepoch') as year," +
                "strftime('%m', timestamp / 1000, 'unixepoch') as month," +
                "strftime('%w', timestamp / 1000, 'unixepoch') as weekday, " +
                "count(*) from repetitions " +
                "where habit = ? " +
                "group by year, month, weekday";

        String[] params = { habit.getId().toString() };

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if(!cursor.moveToFirst()) return new HashMap<>();

        HashMap <Long, Integer[]> map = new HashMap<>();

        do
        {
            int year = Integer.parseInt(cursor.getString(0));
            int month = Integer.parseInt(cursor.getString(1));
            int weekday = (Integer.parseInt(cursor.getString(2)) + 1) % 7;
            int count = cursor.getInt(3);

            Log.d("RepetitionList",
                    String.format("year=%d month=%d weekday=%d", year, month, weekday));

            GregorianCalendar date = DateHelper.getStartOfTodayCalendar();
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month);
            date.set(Calendar.DAY_OF_MONTH, 1);

            long timestamp = date.getTimeInMillis();
            Integer[] list = map.get(timestamp);

            if(list == null)
            {
                list = new Integer[7];
                Arrays.fill(list, 0);
                map.put(timestamp, list);
            }

            list[weekday] = count;
        }
        while (cursor.moveToNext());
        cursor.close();

        return map;
    }
}
