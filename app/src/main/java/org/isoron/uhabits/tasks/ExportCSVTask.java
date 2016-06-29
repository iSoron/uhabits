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

package org.isoron.uhabits.tasks;

import android.support.annotation.*;

import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.util.*;

public class ExportCSVTask extends BaseTask
{
    private ProgressBar progressBar;

    private final List<Habit> selectedHabits;

    private String archiveFilename;

    private ExportCSVTask.Listener listener;

    @NonNull
    private final HabitList habitList;

    public ExportCSVTask(HabitList habitList,
                         List<Habit> selectedHabits,
                         ProgressBar progressBar)
    {
        this.habitList = habitList;
        this.selectedHabits = selectedHabits;
        this.progressBar = progressBar;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void doInBackground()
    {
        try
        {
            File dir = FileUtils.getFilesDir("CSV");
            if (dir == null) return;

            HabitsCSVExporter exporter =
                new HabitsCSVExporter(habitList, selectedHabits, dir);
            archiveFilename = exporter.writeArchive();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        if (listener != null) listener.onExportCSVFinished(archiveFilename);

        if (progressBar != null) progressBar.hide();
        super.onPostExecute(null);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if (progressBar != null) progressBar.show();
    }

    public interface Listener
    {
        void onExportCSVFinished(@Nullable String archiveFilename);
    }
}
