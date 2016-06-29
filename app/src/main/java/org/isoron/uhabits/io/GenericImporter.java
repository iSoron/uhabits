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

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A GenericImporter decides which implementation of AbstractImporter is able to
 * handle a given file and delegates to it the task of importing the data.
 */
public class GenericImporter extends AbstractImporter
{
    List<AbstractImporter> importers;

    public GenericImporter()
    {
        importers = new LinkedList<>();
        importers.add(new LoopDBImporter());
        importers.add(new RewireDBImporter());
        importers.add(new TickmateDBImporter());
        importers.add(new HabitBullCSVImporter());
    }

    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        for (AbstractImporter importer : importers)
            if (importer.canHandle(file)) return true;

        return false;
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException
    {
        for (AbstractImporter importer : importers)
            if (importer.canHandle(file)) importer.importHabitsFromFile(file);
    }
}
