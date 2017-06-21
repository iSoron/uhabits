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

import org.isoron.androidbase.*;
import org.isoron.uhabits.core.db.*;


public class BaseSQLiteOpenHelper extends SQLiteOpenHelper
{
    private final Context context;

    private final int version;

    public BaseSQLiteOpenHelper(@AppContext Context context,
                                String databaseFilename,
                                int version)
    {
        super(context, databaseFilename, null, version);
        this.context = context;
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        MigrationHelper helper =
            new MigrationHelper(new AndroidSQLiteDatabase(db));
        helper.executeMigrations(-1, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        MigrationHelper helper =
            new MigrationHelper(new AndroidSQLiteDatabase(db));
        helper.executeMigrations(oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new UnsupportedDatabaseVersionException();
    }
}
