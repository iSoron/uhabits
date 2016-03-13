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

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.isoron.helpers.DateHelper;

import java.util.List;

public class CheckmarkList
{
    private Habit habit;

    public CheckmarkList(Habit habit)
    {
        this.habit = habit;
    }

    /**
     * Deletes every checkmark that has timestamp either equal or newer than a given timestamp.
     * These checkmarks will be recomputed at the next time they are queried.
     *
     * @param timestamp the timestamp
     */
    public void deleteNewerThan(long timestamp)
    {
        new Delete().from(Checkmark.class)
                .where("habit = ?", habit.getId())
                .and("timestamp >= ?", timestamp)
                .execute();
    }

    /**
     * Returns the values of the checkmarks that fall inside a certain interval of time.
     *
     * The values are returned in an array containing one integer value for each day of the
     * interval. The first entry corresponds to the most recent day in the interval. Each subsequent
     * entry corresponds to one day older than the previous entry. The boundaries of the time
     * interval are included.
     *
     * @param fromTimestamp timestamp for the oldest checkmark
     * @param toTimestamp timestamp for the newest checkmark
     * @return values for the checkmarks inside the given interval
     */
    public int[] getValues(Long fromTimestamp, Long toTimestamp)
    {
        buildCache(fromTimestamp, toTimestamp);
        if(fromTimestamp > toTimestamp) return new int[0];

        String query = "select value, timestamp from Checkmarks where " +
                "habit = ? and timestamp >= ? and timestamp <= ?";

        SQLiteDatabase db = Cache.openDatabase();
        String args[] = { habit.getId().toString(), fromTimestamp.toString(),
                toTimestamp.toString() };
        Cursor cursor = db.rawQuery(query, args);

        long day = DateHelper.millisecondsInOneDay;
        int nDays = (int) ((toTimestamp - fromTimestamp) / day) + 1;
        int[] checks = new int[nDays];

        if (cursor.moveToFirst())
        {
            do
            {
                long timestamp = cursor.getLong(1);
                int offset = (int) ((timestamp - fromTimestamp) / day);
                checks[nDays - offset - 1] = cursor.getInt(0);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return checks;
    }

    /**
     * Computes and returns the values for all the checkmarks, since the oldest repetition of the
     * habit until today. If there are no repetitions at all, returns an empty array.
     *
     * The values are returned in an array containing one integer value for each day since the
     * first repetition of the habit until today. The first entry corresponds to today, the second
     * entry corresponds to yesterday, and so on.
     *
     * @return values for the checkmarks in the interval
     */
    public int[] getAllValues()
    {
        Repetition oldestRep = habit.repetitions.getOldest();
        if(oldestRep == null) return new int[0];

        Long fromTimestamp = oldestRep.timestamp;
        Long toTimestamp = DateHelper.getStartOfToday();

        return getValues(fromTimestamp, toTimestamp);
    }

    /**
     * Computes and stores one checkmark for each day that falls inside the specified interval of
     * time. Days that already have a corresponding checkmark are skipped.
     *
     * @param from timestamp for the beginning of the interval
     * @param to timestamp for the end of the interval
     */
    public void buildCache(long from, long to)
    {
        long day = DateHelper.millisecondsInOneDay;

        Checkmark newestCheckmark = findNewest();
        if(newestCheckmark != null)
            from = Math.max(from, newestCheckmark.timestamp + day);

        if(from > to) return;

        long fromExtended = from - (long) (habit.freqDen) * day;
        List<Repetition> reps = habit.repetitions
                .selectFromTo(fromExtended, to)
                .execute();

        int nDays = (int) ((to - from) / day) + 1;
        int nDaysExtended = (int) ((to - fromExtended) / day) + 1;
        int checks[] = new int[nDaysExtended];

        for (Repetition rep : reps)
        {
            int offset = (int) ((rep.timestamp - fromExtended) / day);
            checks[nDaysExtended - offset - 1] = Checkmark.CHECKED_EXPLICITLY;
        }

        for (int i = 0; i < nDays; i++)
        {
            int counter = 0;

            for (int j = 0; j < habit.freqDen; j++)
                if (checks[i + j] == 2) counter++;

            if (counter >= habit.freqNum)
                if(checks[i] != Checkmark.CHECKED_EXPLICITLY)
                    checks[i] = Checkmark.CHECKED_IMPLICITLY;
        }

        ActiveAndroid.beginTransaction();

        try
        {
            for (int i = 0; i < nDays; i++)
            {
                Checkmark c = new Checkmark();
                c.habit = habit;
                c.timestamp = to - i * day;
                c.value = checks[i];
                c.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * Returns newest checkmark that has already been computed. Ignores any checkmark that has
     * timestamp in the future. This does not update the cache.
     */
    private Checkmark findNewest()
    {
        return new Select().from(Checkmark.class)
                .where("habit = ?", habit.getId())
                .and("timestamp <= ?", DateHelper.getStartOfToday())
                .orderBy("timestamp desc")
                .limit(1)
                .executeSingle();
    }

    /**
     * Returns the checkmark for today.
     */
    public Checkmark getToday()
    {
        long today = DateHelper.getStartOfToday();
        buildCache(today, today);
        return findNewest();
    }

    /**
     * Returns the value of today's checkmark.
     */
    public int getTodayValue()
    {
        return getToday().value;
    }
}
