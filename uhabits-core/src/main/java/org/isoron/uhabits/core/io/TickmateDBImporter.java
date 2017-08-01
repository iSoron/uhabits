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

import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

/**
 * Class that imports data from database files exported by Tickmate.
 */
public class TickmateDBImporter extends AbstractImporter
{
    private ModelFactory modelFactory;

    @NonNull
    private final DatabaseOpener opener;

    @Inject
    public TickmateDBImporter(@NonNull HabitList habits,
                              @NonNull ModelFactory modelFactory,
                              @NonNull DatabaseOpener opener)
    {
        super(habits);
        this.modelFactory = modelFactory;
        this.opener = opener;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        Database db = opener.open(file);
        Cursor c = db.query("select count(*) from SQLITE_MASTER " +
                            "where name='tracks' or name='track2groups'");

        boolean result = (c.moveToNext() && c.getInt(0) == 2);

        c.close();
        db.close();
        return result;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
        final Database db = opener.open(file);
        db.beginTransaction();
        createHabits(db);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private void createCheckmarks(@NonNull Database db,
                                  @NonNull Habit habit,
                                  int tickmateTrackId)
    {
        Cursor c = null;

        try
        {
            String[] params = {Integer.toString(tickmateTrackId)};
            c = db.query(
                "select distinct year, month, day from ticks where _track_id=?",
                params);
            if (!c.moveToNext()) return;

            do
            {
                int year = c.getInt(0);
                int month = c.getInt(1);
                int day = c.getInt(2);

                GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
                cal.set(year, month, day);

                habit.getRepetitions().toggle(new Timestamp(cal));
            } while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }

    private void createHabits(Database db)
    {
        Cursor c = null;

        try
        {
            c = db.query("select _id, name, description from tracks",
                new String[0]);
            if (!c.moveToNext()) return;

            do
            {
                int id = c.getInt(0);
                String name = c.getString(1);
                String description = c.getString(2);

                Habit habit = modelFactory.buildHabit();
                habit.setName(name);
                habit.setDescription(description);
                habit.setFrequency(Frequency.DAILY);
                habitList.add(habit);

                createCheckmarks(db, habit, id);

            } while (c.moveToNext());
        }
        finally
        {
            if (c != null) c.close();
        }
    }
}
