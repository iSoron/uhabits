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

import android.database.sqlite.*;
import android.support.annotation.*;

import com.activeandroid.*;
import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

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
    public void add(List<Checkmark> checkmarks)
    {
        String query =
            "insert into Checkmarks(habit, timestamp, value) values (?,?,?)";

        SQLiteDatabase db = Cache.openDatabase();
        db.beginTransaction();
        try
        {
            SQLiteStatement statement = db.compileStatement(query);

            for (Checkmark c : checkmarks)
            {
                statement.bindLong(1, habit.getId());
                statement.bindLong(2, c.getTimestamp());
                statement.bindLong(3, c.getValue());
                statement.execute();
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    @Override
    public List<Checkmark> getByInterval(long fromTimestamp, long toTimestamp)
    {
        computeAll();

        List<CheckmarkRecord> records = select()
            .and("timestamp >= ?", fromTimestamp)
            .and("timestamp <= ?", toTimestamp)
            .execute();

        return toCheckmarks(records);
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
    @Nullable
    protected Checkmark getNewestComputed()
    {
        CheckmarkRecord record = select().limit(1).executeSingle();
        if (record == null) return null;
        return record.toCheckmark();
    }

    @NonNull
    private From select()
    {
        return new Select()
            .from(CheckmarkRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp <= ?", DateUtils.getStartOfToday())
            .orderBy("timestamp desc");
    }

    @NonNull
    private List<Checkmark> toCheckmarks(@NonNull List<CheckmarkRecord> records)
    {
        List<Checkmark> checkmarks = new LinkedList<>();
        for (CheckmarkRecord r : records) checkmarks.add(r.toCheckmark());
        return checkmarks;
    }
}
