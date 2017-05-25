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

package org.isoron.uhabits.io;

import android.support.annotation.*;

import org.isoron.uhabits.models.*;

import java.io.*;
import java.util.*;

/**
 * AbstractImporter is the base class for all classes that import data from
 * files into the app.
 */
public abstract class AbstractImporter
{
    protected final HabitList habits;

    public AbstractImporter(HabitList habits)
    {
        this.habits = habits;
    }

    public abstract boolean canHandle(@NonNull File file) throws IOException;

    public abstract void importHabitsFromFile(@NonNull File file) throws IOException;

    public static boolean isSQLite3File(@NonNull File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);

        byte[] sqliteHeader = "SQLite format 3".getBytes();
        byte[] buffer = new byte[sqliteHeader.length];


        int count = fis.read(buffer);
        if(count < sqliteHeader.length) return false;

        return Arrays.equals(buffer, sqliteHeader);
    }
}