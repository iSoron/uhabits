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

package org.isoron.uhabits.models.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.CheckmarkList;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.DateUtils;

/**
 * Implementation of a {@link CheckmarkList} that is backed by SQLite.
 */
public class SQLiteCheckmarkList extends CheckmarkList
{
    public SQLiteCheckmarkList(Habit habit)
    {
        super(habit);
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        new Delete()
            .from(CheckmarkRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp >= ?", timestamp)
            .execute();

        observable.notifyListeners();
    }

    @Override
    @NonNull
    public int[] getValues(long fromTimestamp, long toTimestamp)
    {
        compute(fromTimestamp, toTimestamp);

        if (fromTimestamp > toTimestamp) return new int[0];

        String query = "select value, timestamp from Checkmarks where " +
                       "habit = ? and timestamp >= ? and timestamp <= ?";

        SQLiteDatabase db = Cache.openDatabase();
        String args[] = {
            habit.getId().toString(),
            Long.toString(fromTimestamp),
            Long.toString(toTimestamp)
        };
        Cursor cursor = db.rawQuery(query, args);

        long day = DateUtils.millisecondsInOneDay;
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

    @Override
    @Nullable
    protected Checkmark getNewest()
    {
        CheckmarkRecord record = new Select()
            .from(CheckmarkRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp <= ?", DateUtils.getStartOfToday())
            .orderBy("timestamp desc")
            .limit(1)
            .executeSingle();

        return record.toCheckmark();
    }

    @Override
    protected void insert(long timestamps[], int values[])
    {
        String query =
            "insert into Checkmarks(habit, timestamp, value) values (?,?,?)";

        SQLiteDatabase db = Cache.openDatabase();
        db.beginTransaction();
        try
        {
            SQLiteStatement statement = db.compileStatement(query);

            for (int i = 0; i < timestamps.length; i++)
            {
                statement.bindLong(1, habit.getId());
                statement.bindLong(2, timestamps[i]);
                statement.bindLong(3, values[i]);
                statement.execute();
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }
}
