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

import org.isoron.uhabits.utils.*;

import java.io.*;

public class ExportDBTask implements Task
{
    private String filename;

    @NonNull
    private final Listener listener;

    public ExportDBTask(@NonNull Listener listener)
    {
        this.listener = listener;
    }

    @Override
    public void doInBackground()
    {
        filename = null;

        try
        {
            File dir = FileUtils.getFilesDir("Backups");
            if (dir == null) return;

            filename = DatabaseUtils.saveDatabaseCopy(dir);
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
