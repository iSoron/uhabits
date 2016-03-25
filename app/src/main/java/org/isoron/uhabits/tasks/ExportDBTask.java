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

import java.io.File;
import java.io.IOException;

public class ExportDBTask extends AsyncTask<Void, Void, Void>
{
    public interface Listener
    {
        void onExportDBFinished(@Nullable String filename);
    }

    private ProgressBar progressBar;
    private String filename;
    private Listener listener;

    public ExportDBTask(ProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
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
            listener.onExportDBFinished(filename);
        
        if(progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        filename = null;

        try
        {
            File dir = DatabaseHelper.getFilesDir("Backups");
            if(dir == null) return null;

            filename = DatabaseHelper.saveDatabaseCopy(dir);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
