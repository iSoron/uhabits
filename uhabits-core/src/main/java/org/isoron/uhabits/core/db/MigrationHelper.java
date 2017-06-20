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

package org.isoron.uhabits.core.db;

import android.support.annotation.*;

import java.io.*;
import java.util.*;

public class MigrationHelper
{
    private final FileOpener opener;

    private final Database db;

    public MigrationHelper(@NonNull FileOpener opener, @NonNull Database db)
    {
        this.opener = opener;
        this.db = db;
    }

    public void executeMigrations(int oldVersion, int newVersion)
    {
        try
        {
            for (int v = oldVersion + 1; v <= newVersion; v++)
            {
                String fname = String.format(Locale.US, "migrations/%d.sql", v);
                InputStream stream = opener.open(fname);
                for (String command : SQLParser.parse(stream))
                    db.execute(command);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public interface FileOpener
    {
        InputStream open(String filename);
    }
}
