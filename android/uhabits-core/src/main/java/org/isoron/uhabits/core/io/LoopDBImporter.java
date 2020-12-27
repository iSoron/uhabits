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

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.records.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

import static org.isoron.uhabits.core.ConstantsKt.*;

/**
 * Class that imports data from database files exported by Loop Habit Tracker.
 */
public class LoopDBImporter extends AbstractImporter
{
    @NonNull
    private final ModelFactory modelFactory;

    @NonNull
    private final DatabaseOpener opener;
    @NonNull
    private final CommandRunner runner;

    @Inject
    public LoopDBImporter(@AppScope @NonNull HabitList habitList,
                          @AppScope @NonNull ModelFactory modelFactory,
                          @AppScope @NonNull DatabaseOpener opener,
                          @AppScope @NonNull CommandRunner runner)
    {
        super(habitList);
        this.modelFactory = modelFactory;
        this.opener = opener;
        this.runner = runner;
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        if (!isSQLite3File(file)) return false;

        Database db = opener.open(file);
        boolean canHandle = true;

        Cursor c = db.query("select count(*) from SQLITE_MASTER " +
                "where name='Habits' or name='Repetitions'");

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
    {
        Database db = opener.open(file);
        MigrationHelper helper = new MigrationHelper(db);
        helper.migrateTo(DATABASE_VERSION);

        Repository<HabitRecord> habitsRepository;
        Repository<EntryRecord> entryRepository;
        habitsRepository = new Repository<>(HabitRecord.class, db);
        entryRepository = new Repository<>(EntryRecord.class, db);

        List<HabitRecord> habitRecords = habitsRepository.findAll("order by position");
        for (HabitRecord habitRecord : habitRecords)
        {
            List<EntryRecord> entryRecords =
                    entryRepository.findAll("where habit = ?",
                            habitRecord.id.toString());

            Habit habit = habitList.getByUUID(habitRecord.uuid);
            Command command;
            if (habit == null)
            {
                habit = modelFactory.buildHabit();
                habitRecord.id = null;
                habitRecord.copyTo(habit);
                command = new CreateHabitCommand(modelFactory, habitList, habit);
                command.run();
            }
            else
            {
                Habit modified = modelFactory.buildHabit();
                habitRecord.id = habit.getId();
                habitRecord.copyTo(modified);
                command = new EditHabitCommand(habitList, habit.getId(), modified);
                command.run();
            }

            // Reload saved version of the habit
            habit = habitList.getByUUID(habitRecord.uuid);

            for (EntryRecord r : entryRecords)
            {
                Timestamp t = new Timestamp(r.timestamp);
                Entry existingEntry = habit.getOriginalEntries().get(t);
                if (existingEntry.getValue() != r.value)
                    new CreateRepetitionCommand(habitList, habit, t, r.value).run();
            }

            runner.notifyListeners(command);
        }
        db.close();
    }
}
