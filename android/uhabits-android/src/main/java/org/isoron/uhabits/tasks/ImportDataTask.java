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

package org.isoron.uhabits.tasks;

import android.util.*;

import androidx.annotation.NonNull;

import com.google.auto.factory.*;

import org.isoron.uhabits.core.io.*;
import org.isoron.uhabits.core.models.ModelFactory;
import org.isoron.uhabits.core.models.sqlite.SQLModelFactory;
import org.isoron.uhabits.core.tasks.*;

import java.io.*;

@AutoFactory(allowSubclasses = true)
public class ImportDataTask implements Task
{
    public static final int FAILED = 3;

    public static final int NOT_RECOGNIZED = 2;

    public static final int SUCCESS = 1;

    private int result;

    @NonNull
    private final File file;

    private GenericImporter importer;

    private SQLModelFactory modelFactory;

    @NonNull
    private final Listener listener;

    public ImportDataTask(@Provided @NonNull GenericImporter importer,
                          @Provided @NonNull ModelFactory modelFactory,
                          @NonNull File file,
                          @NonNull Listener listener)
    {
        this.importer = importer;
        this.modelFactory = (SQLModelFactory) modelFactory;
        this.listener = listener;
        this.file = file;
    }

    @Override
    public void doInBackground()
    {
        modelFactory.db.beginTransaction();

        try
        {
            if (importer.canHandle(file))
            {
                importer.importHabitsFromFile(file);
                result = SUCCESS;
                modelFactory.db.setTransactionSuccessful();
            }
            else
            {
                result = NOT_RECOGNIZED;
            }
        }
        catch (Exception e)
        {
            result = FAILED;
            Log.e("ImportDataTask", "Import failed", e);
        }

        modelFactory.db.endTransaction();
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