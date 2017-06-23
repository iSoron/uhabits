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

import android.support.annotation.*;

import org.isoron.uhabits.core.database.*;

public class AndroidCursor implements Cursor
{
    private android.database.Cursor cursor;

    public AndroidCursor(android.database.Cursor cursor)
    {
        this.cursor = cursor;
    }

    @Override
    public void close()
    {
        cursor.close();
    }

    @Override
    public boolean moveToNext()
    {
        return cursor.moveToNext();
    }

    @Override
    @Nullable
    public Integer getInt(int index)
    {
        if(cursor.isNull(index)) return null;
        else return cursor.getInt(index);
    }

    @Override
    @Nullable
    public Long getLong(int index)
    {
        if(cursor.isNull(index)) return null;
        else return cursor.getLong(index);
    }

    @Override
    @Nullable
    public Double getDouble(int index)
    {
        if(cursor.isNull(index)) return null;
        else return cursor.getDouble(index);
    }

    @Override
    @Nullable
    public String getString(int index)
    {
        if(cursor.isNull(index)) return null;
        else return cursor.getString(index);
    }
}
