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

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.io.HabitsCSVExporter;
import org.isoron.uhabits.models.Habit;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExportCSVTask extends BaseTask
{
    public interface Listener
    {
        void onExportCSVFinished(@Nullable String archiveFilename);
    }

    private ProgressBar progressBar;
    private final List<Habit> selectedHabits;
    private String archiveFilename;
    private ExportCSVTask.Listener listener;

    public ExportCSVTask(List<Habit> selectedHabits, ProgressBar progressBar)
    {
        this.selectedHabits = selectedHabits;
        this.progressBar = progressBar;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        if(progressBar != null)
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        if(listener != null)
            listener.onExportCSVFinished(archiveFilename);

        if(progressBar != null)
            progressBar.setVisibility(View.GONE);

        super.onPostExecute(null);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        try
        {
            File dir = DatabaseHelper.getFilesDir("CSV");
            if(dir == null) return null;

            HabitsCSVExporter exporter = new HabitsCSVExporter(selectedHabits, dir);
            archiveFilename = exporter.writeArchive();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
