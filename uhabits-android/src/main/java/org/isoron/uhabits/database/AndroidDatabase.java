/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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
 *
 *
 */

package org.isoron.uhabits.database;

import android.content.*;
import android.database.sqlite.*;

import org.isoron.uhabits.core.database.*;

import java.util.*;

public class AndroidDatabase implements Database
{
    private final SQLiteDatabase db;

    public AndroidDatabase(SQLiteDatabase db)
    {
        this.db = db;
    }

    @Override
    public Cursor query(String query, String... params)
    {
        return new AndroidCursor(db.rawQuery(query, params));
    }

    @Override
    public void execute(String query, Object... params)
    {
        db.execSQL(query, params);
    }

    @Override
    public void beginTransaction()
    {
        db.beginTransaction();
    }

    @Override
    public void setTransactionSuccessful()
    {
        db.setTransactionSuccessful();
    }

    @Override
    public void endTransaction()
    {
        db.endTransaction();
    }

    @Override
    public void close()
    {
        db.close();
    }

    @Override
    public int getVersion()
    {
        return db.getVersion();
    }

    @Override
    public int update(String tableName,
                      Map<String, Object> map,
                      String where,
                      String... params)
    {
        ContentValues values = mapToContentValues(map);
        return db.update(tableName, values, where, params);
    }

    @Override
    public Long insert(String tableName, Map<String, Object> map)
    {
        ContentValues values = mapToContentValues(map);
        return db.insert(tableName, null, values);
    }

    @Override
    public void delete(String tableName, String where, String... params)
    {
        db.delete(tableName, where, params);
    }

    private ContentValues mapToContentValues(Map<String, Object> map)
    {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() == null)
                values.putNull(entry.getKey());
            else if (entry.getValue() instanceof Integer)
                values.put(entry.getKey(), (Integer) entry.getValue());
            else if (entry.getValue() instanceof Long)
                values.put(entry.getKey(), (Long) entry.getValue());
            else if (entry.getValue() instanceof Double)
                values.put(entry.getKey(), (Double) entry.getValue());
            else if (entry.getValue() instanceof String)
                values.put(entry.getKey(), (String) entry.getValue());
            else throw new IllegalStateException(
                    "unsupported type: " + entry.getValue());
        }

        return values;
    }
}
