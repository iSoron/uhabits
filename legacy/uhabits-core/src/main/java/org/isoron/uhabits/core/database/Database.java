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
 */

package org.isoron.uhabits.core.database;

import java.util.*;

public interface Database
{
    Cursor query(String query, String... params);

    default void query(String query, ProcessCallback callback)
    {
        try (Cursor c = query(query)) {
            c.moveToNext();
            callback.process(c);
        }
    }

    int update(String tableName,
               Map<String, Object> values,
               String where,
               String... params);

    Long insert(String tableName, Map<String, Object> values);

    void delete(String tableName, String where, String... params);

    void execute(String query, Object... params);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

    void close();

    int getVersion();

    interface ProcessCallback
    {
        void process(Cursor cursor);
    }
}
