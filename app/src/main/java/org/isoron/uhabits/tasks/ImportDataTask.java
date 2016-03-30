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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import org.isoron.uhabits.io.GenericImporter;

import java.io.File;

public class ImportDataTask extends BaseTask
{
    public static final int SUCCESS = 1;
    public static final int NOT_RECOGNIZED = 2;
    public static final int FAILED = 3;

    public interface Listener
    {
        void onImportFinished(int result);
    }

    @Nullable
    private final ProgressBar progressBar;

    @NonNull
    private final File file;

    @Nullable
    private Listener listener;

    int result;

    public ImportDataTask(@NonNull File file, @Nullable ProgressBar progressBar)
    {
        this.file = file;
        this.progressBar = progressBar;
    }

    public void setListener(@Nullable Listener listener)
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
        if(progressBar != null)
            progressBar.setVisibility(View.GONE);

        if(listener != null) listener.onImportFinished(result);

        super.onPostExecute(null);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        try
        {
            GenericImporter importer = new GenericImporter();
            if(importer.canHandle(file))
            {
                importer.importHabitsFromFile(file);
                result = SUCCESS;
            }
            else
            {
                result = NOT_RECOGNIZED;
            }
        }
        catch (Exception e)
        {
            result = FAILED;
            e.printStackTrace();
        }

        return null;
    }
}