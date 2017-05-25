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

import android.content.Context;
import android.support.annotation.*;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import org.isoron.uhabits.AppContext;
import org.isoron.uhabits.activities.ActivityContext;
import org.isoron.uhabits.utils.*;

import java.io.*;

@AutoFactory(allowSubclasses = true)
public class ExportDBTask implements Task
{
    private String filename;

    @NonNull
    private Context context;

    @NonNull
    private final Listener listener;

    public ExportDBTask(@Provided @AppContext @NonNull Context context, @NonNull Listener listener)
    {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void doInBackground()
    {
        filename = null;

        try
        {
            File dir = FileUtils.getFilesDir(context, "Backups");
            if (dir == null) return;

            filename = DatabaseUtils.saveDatabaseCopy(context, dir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostExecute()
    {
        listener.onExportDBFinished(filename);
    }

    public interface Listener
    {
        void onExportDBFinished(@Nullable String filename);
    }
}
