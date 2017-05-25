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

import com.google.auto.factory.*;

import org.isoron.uhabits.io.*;

import java.io.*;

@AutoFactory(allowSubclasses = true)
public class ImportDataTask implements Task
{
    public static final int FAILED = 3;

    public static final int NOT_RECOGNIZED = 2;

    public static final int SUCCESS = 1;

    int result;

    @NonNull
    private final File file;

    private GenericImporter importer;

    @NonNull
    private final Listener listener;

    public ImportDataTask(@Provided @NonNull GenericImporter importer,
                          @NonNull File file,
                          @NonNull Listener listener)
    {
        this.importer = importer;
        this.listener = listener;
        this.file = file;
    }

    @Override
    public void doInBackground()
    {
        try
        {
            if (importer.canHandle(file))
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
    }

    @Override
    public void onPostExecute()
    {
        listener.onImportDataFinished(result);
    }

    public interface Listener
    {
        void onImportDataFinished(int result);
    }
}