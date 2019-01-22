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

package org.isoron.uhabits.core.tasks;

import android.support.annotation.*;

import com.google.auto.factory.*;

import org.isoron.uhabits.core.io.*;
import org.isoron.uhabits.core.models.*;

import java.io.*;
import java.util.*;

@AutoFactory(allowSubclasses = true)
public class ExportCSVTask implements Task
{
    private String archiveFilename;

    @NonNull
    private final List<Habit> selectedHabits;

    private File outputDir;

    @NonNull
    private final ExportCSVTask.Listener listener;

    @NonNull
    private final HabitList habitList;

    public ExportCSVTask(@Provided @NonNull HabitList habitList,
                         @NonNull List<Habit> selectedHabits,
                         @NonNull File outputDir,
                         @NonNull Listener listener)
    {
        this.listener = listener;
        this.habitList = habitList;
        this.selectedHabits = selectedHabits;
        this.outputDir = outputDir;
    }

    @Override
    public void doInBackground()
    {
        try
        {
            HabitsCSVExporter exporter;
            exporter = new HabitsCSVExporter(habitList, selectedHabits, outputDir);
            archiveFilename = exporter.writeArchive();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostExecute()
    {
        listener.onExportCSVFinished(archiveFilename);
    }

    public interface Listener
    {
        void onExportCSVFinished(@Nullable String archiveFilename);
    }
}
