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

package org.isoron.uhabits;

import android.content.*;
import android.database.sqlite.*;

import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.database.*;


public class HabitsDatabaseOpener extends SQLiteOpenHelper
{
    private final int version;

    private MigrationHelper helper;

    public HabitsDatabaseOpener(Context context,
                                String databaseFilename,
                                int version)
    {
        super(context, databaseFilename, null, version);
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        onUpgrade(db, 8, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion < 8) throw new UnsupportedDatabaseVersionException();
        helper = new MigrationHelper(new AndroidDatabase(db));
        helper.executeMigrations(oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new UnsupportedDatabaseVersionException();
    }
}
