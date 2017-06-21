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

package org.isoron.uhabits.core.io;

import android.support.annotation.*;

import org.apache.commons.io.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;

import java.io.*;

import javax.inject.*;

import static org.isoron.uhabits.core.Config.DATABASE_VERSION;

/**
 * Class that imports data from database files exported by Loop Habit Tracker.
 */
public class LoopDBImporter extends AbstractImporter
{
    @NonNull
    private final DatabaseOpener opener;

    @Inject
    public LoopDBImporter(@NonNull HabitList habits,
                          @NonNull DatabaseOpener opener)
    {
        super(habits);
        this.opener = opener;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        Database db = opener.open(file);
        boolean canHandle = true;

        Cursor c = db.select("select count(*) from SQLITE_MASTER " +
                             "where name='Checkmarks' or name='Repetitions'");

        if (!c.moveToNext() || c.getInt(0) != 2)
        {
//            Log.w("LoopDBImporter", "Cannot handle file: tables not found");
            canHandle = false;
        }

        if (db.getVersion() > DATABASE_VERSION)
        {
//            Log.w("LoopDBImporter", String.format(
//                "Cannot handle file: incompatible version: %d > %d",
//                db.getVersion(), DATABASE_VERSION));
            canHandle = false;
        }

        c.close();
        db.close();
        return canHandle;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
//        DatabaseUtils.dispose();
        File originalDB = opener.getProductionDatabaseFile();
        FileUtils.copyFile(file, originalDB);
//        DatabaseUtils.initializeDatabase(context);
    }
}
