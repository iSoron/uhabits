/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
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

    public void deleteNewerThan(long timestamp)
    {
        new Delete().from(Checkmark.class)
                .where("habit = ?", habit.getId())
                .and("timestamp >= ?", timestamp)
                .execute();
    }

    public int[] getValues(Long fromTimestamp, Long toTimestamp)
    {
        rebuild();

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

    public int[] getAllValues()
    {
        Repetition oldestRep = habit.repetitions.getOldest();
        if(oldestRep == null) return new int[0];

        Long toTimestamp = DateHelper.getStartOfToday();
        Long fromTimestamp = oldestRep.timestamp;
        return getValues(fromTimestamp, toTimestamp);
    }

    public void rebuild()
    {
        long beginning;
        long today = DateHelper.getStartOfToday();
        long day = DateHelper.millisecondsInOneDay;

        Checkmark newestCheckmark = getNewest();
        if (newestCheckmark == null)
        {
            Repetition oldestRep = habit.repetitions.getOldest();
            if (oldestRep == null) return;

            beginning = oldestRep.timestamp;
        }
        else
        {
            beginning = newestCheckmark.timestamp + day;
        }

        if (beginning > today) return;

        long beginningExtended = beginning - (long) (habit.freqDen) * day;
        List<Repetition> reps = habit.repetitions.selectFromTo(beginningExtended, today).execute();

        int nDays = (int) ((today - beginning) / day) + 1;
        int nDaysExtended = (int) ((today - beginningExtended) / day) + 1;

        int checks[] = new int[nDaysExtended];

        // explicit checks
        for (Repetition rep : reps)
        {
            int offset = (int) ((rep.timestamp - beginningExtended) / day);
            checks[nDaysExtended - offset - 1] = 2;
        }

        // implicit checks
        for (int i = 0; i < nDays; i++)
        {
            int counter = 0;

            for (int j = 0; j < habit.freqDen; j++)
                if (checks[i + j] == 2) counter++;

            if (counter >= habit.freqNum) checks[i] = Math.max(checks[i], 1);
        }

        ActiveAndroid.beginTransaction();

        try
        {
            for (int i = 0; i < nDays; i++)
            {
                Checkmark c = new Checkmark();
                c.habit = habit;
                c.timestamp = today - i * day;
                c.value = checks[i];
                c.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    public Checkmark getNewest()
    {
        return new Select().from(Checkmark.class)
                .where("habit = ?", habit.getId())
                .orderBy("timestamp desc")
                .limit(1)
                .executeSingle();
    }

    public int getCurrentValue()
    {
        rebuild();
        Checkmark c = getNewest();

        if(c != null) return c.value;
        else return 0;
    }
}
