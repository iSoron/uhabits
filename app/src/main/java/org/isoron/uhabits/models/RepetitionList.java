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

import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.isoron.helpers.DateHelper;

import java.util.Arrays;
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
                .and("timestamp <= ?", DateHelper.getStartOfToday())
                .orderBy("timestamp");
    }

    protected From selectFromTo(long timeFrom, long timeTo)
    {
        return select().and("timestamp >= ?", timeFrom).and("timestamp <= ?", timeTo);
    }

    /**
     * Checks whether there is a repetition at a given timestamp.
     *
     * @param timestamp the timestamp to check
     * @return true if there is a repetition
     */
    public boolean contains(long timestamp)
    {
        int count = select().where("timestamp = ?", timestamp).count();
        return (count > 0);
    }

    /**
     * Deletes the repetition at a given timestamp, if it exists.
     *
     * @param timestamp the timestamp of the repetition to delete
     */
    public void delete(long timestamp)
    {
        new Delete().from(Repetition.class)
                .where("habit = ?", habit.getId())
                .and("timestamp = ?", timestamp)
                .execute();
    }

    /**
     * Toggles the repetition at a certain timestamp. That is, deletes the repetition if it exists
     * or creates one if it does not.
     *
     * @param timestamp the timestamp of the repetition to toggle
     */
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

    /**
     * Returns the oldest repetition for the habit. If there is no repetition, returns null.
     * Repetitions in the future are discarded.
     *
     * @return oldest repetition for the habit
     */
    public Repetition getOldest()
    {
        return (Repetition) select().limit(1).executeSingle();
    }

    /**
     * Returns the total number of repetitions for each month, from the first repetition until
     * today, grouped by day of week. The repetitions are returned in a HashMap. The key is the
     * timestamp for the first day of the month, at midnight (00:00). The value is an integer
     * array with 7 entries. The first entry contains the total number of repetitions during
     * the specified month that occurred on a Saturday. The second entry corresponds to Sunday,
     * and so on. If there are no repetitions during a certain month, the value is null.
     *
     * @return total number of repetitions by month versus day of week
     */
    public HashMap<Long, Integer[]> getWeekdayFrequency()
    {
        Repetition oldestRep = getOldest();
        if(oldestRep == null) return new HashMap<>();

        String query = "select strftime('%Y', timestamp / 1000, 'unixepoch') as year," +
                "strftime('%m', timestamp / 1000, 'unixepoch') as month," +
                "strftime('%w', timestamp / 1000, 'unixepoch') as weekday, " +
                "count(*) from repetitions " +
                "where habit = ? and timestamp <= ? " +
                "group by year, month, weekday";

        String[] params = { habit.getId().toString(),
                Long.toString(DateHelper.getStartOfToday()) };

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if(!cursor.moveToFirst()) return new HashMap<>();

        HashMap <Long, Integer[]> map = new HashMap<>();
        GregorianCalendar date = DateHelper.getStartOfTodayCalendar();

        do
        {
            int year = Integer.parseInt(cursor.getString(0));
            int month = Integer.parseInt(cursor.getString(1));
            int weekday = (Integer.parseInt(cursor.getString(2)) + 1) % 7;
            int count = cursor.getInt(3);

            date.set(year, month - 1, 1);
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
