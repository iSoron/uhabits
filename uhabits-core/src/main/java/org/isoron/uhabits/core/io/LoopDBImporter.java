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
import org.isoron.uhabits.core.models.sqlite.records.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

import static org.isoron.uhabits.core.Config.*;

/**
 * Class that imports data from database files exported by Loop Habit Tracker.
 */
public class LoopDBImporter extends AbstractImporter
{
    @NonNull
    private final ModelFactory modelFactory;

    @NonNull
    private final DatabaseOpener opener;

    @Inject
    public LoopDBImporter(@NonNull HabitList habitList,
                          @NonNull ModelFactory modelFactory,
                          @NonNull DatabaseOpener opener)
    {
        super(habitList);
        this.modelFactory = modelFactory;
        this.opener = opener;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        Database db = opener.open(file);
        boolean canHandle = true;

        Cursor c = db.query("select count(*) from SQLITE_MASTER " +
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
    public synchronized void importHabitsFromFile(@NonNull File file)
        throws IOException
    {
        Database db = opener.open(file);
        MigrationHelper helper = new MigrationHelper(db);
        helper.migrateTo(DATABASE_VERSION);

        Repository<HabitRecord> habitsRepository;
        Repository<RepetitionRecord> repsRepository;
        habitsRepository = new Repository<>(HabitRecord.class, db);
        repsRepository = new Repository<>(RepetitionRecord.class, db);

        for (HabitRecord habitRecord : habitsRepository.findAll(
            "order by position"))
        {
            Habit h = modelFactory.buildHabit();
            habitRecord.copyTo(h);
            h.setId(null);
            habitList.add(h);

            List<RepetitionRecord> reps =
                repsRepository.findAll("where habit = ?",
                    habitRecord.id.toString());

            for (RepetitionRecord r : reps)
                h.getRepetitions().toggle(r.timestamp, r.value);
        }
    }
}
