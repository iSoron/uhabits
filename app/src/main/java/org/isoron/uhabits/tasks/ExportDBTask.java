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

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.isoron.uhabits.R;
import org.isoron.uhabits.ReplayableActivity;
import org.isoron.uhabits.helpers.DatabaseHelper;

import java.io.File;
import java.io.IOException;

public class ExportDBTask extends AsyncTask<Void, Void, Void>
{
    private final ReplayableActivity activity;
    private ProgressBar progressBar;
    private String filename;

    public ExportDBTask(ReplayableActivity activity, ProgressBar progressBar)
    {
        this.progressBar = progressBar;
        this.activity = activity;
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
        if(filename != null)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));

            activity.startActivity(intent);
        }
        else
        {
            activity.showToast(R.string.could_not_export);
        }
        
        if(progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        filename = null;

        try
        {
            File dir = DatabaseHelper.getFilesDir(activity, "Backups");
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
