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

import android.support.annotation.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class MigrationHelper
{
    private static final Logger LOGGER =
        Logger.getLogger(MigrationHelper.class.getName());

    private final Database db;

    public MigrationHelper(@NonNull Database db)
    {
        this.db = db;
    }

    public void migrateTo(int newVersion)
    {
        try
        {
            for (int v = db.getVersion() + 1; v <= newVersion; v++)
            {
                String fname = String.format(Locale.US, "/migrations/%02d.sql", v);
                for (String command : SQLParser.parse(open(fname)))
                    db.execute(command);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private InputStream open(String fname) throws IOException
    {
        InputStream resource = getClass().getResourceAsStream(fname);
        if(resource != null) return resource;

        // Workaround for bug in Android Studio / IntelliJ. Removing this
        // causes unit tests to fail when run from within the IDE, although
        // everything works fine from the command line.
        File file = new File("uhabits-core/src/main/resources/" + fname);
        if(file.exists()) return new FileInputStream(file);

        throw new RuntimeException("resource not found: " + fname);
    }
}
