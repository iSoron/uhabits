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

import android.database.*;
import android.database.sqlite.*;
import android.support.annotation.*;

import com.activeandroid.*;

import org.isoron.uhabits.models.sqlite.records.*;

import java.util.*;

public class SQLiteUtils<T extends SQLiteRecord>
{
    private Class klass;

    public SQLiteUtils(Class klass)
    {
        this.klass = klass;
    }

    @NonNull
    public List<T> query(String query, String params[])
    {
        SQLiteDatabase db = Cache.openDatabase();
        try (Cursor c = db.rawQuery(query, params))
        {
            return cursorToMultipleRecords(c);
        }
    }

    @Nullable
    public T querySingle(String query, String params[])
    {
        SQLiteDatabase db = Cache.openDatabase();
        try(Cursor c = db.rawQuery(query, params))
        {
            if (!c.moveToNext()) return null;
            return cursorToSingleRecord(c);
        }
    }

    @NonNull
    private List<T> cursorToMultipleRecords(Cursor c)
    {
        List<T> records = new LinkedList<>();
        while (c.moveToNext()) records.add(cursorToSingleRecord(c));
        return records;
    }

    @NonNull
    private T cursorToSingleRecord(Cursor c)
    {
        try
        {
            T record = (T) klass.newInstance();
            record.copyFrom(c);
            return record;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
