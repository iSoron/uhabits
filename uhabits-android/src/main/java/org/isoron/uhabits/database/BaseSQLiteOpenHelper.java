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

import java.io.*;
import java.util.*;


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
        executeMigrations(db, -1, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        executeMigrations(db, oldVersion, newVersion);
    }

    private void executeMigrations(SQLiteDatabase db,
                                   int oldVersion,
                                   int newVersion)
    {
        try
        {
            for (int v = oldVersion + 1; v <= newVersion; v++)
            {
                String fname = String.format(Locale.US, "migrations/%d.sql", v);
                InputStream stream = context.getAssets().open(fname);
                for (String command : SQLParser.parse(stream))
                    db.execSQL(command);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new UnsupportedDatabaseVersionException();
    }
}
