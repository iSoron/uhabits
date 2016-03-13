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

package org.isoron.uhabits;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

import java.io.File;

public class HabitsApplication extends Application
{
    private boolean isTestMode()
    {
        try
        {
            getClassLoader().loadClass("org.isoron.uhabits.unit.models.HabitTest");
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    private void deleteDB(String databaseFilename)
    {
        File databaseFile = new File(String.format("%s/../databases/%s",
                getApplicationContext().getFilesDir().getPath(), databaseFilename));

        if(databaseFile.exists()) databaseFile.delete();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        String databaseFilename = BuildConfig.databaseFilename;

        if (isTestMode())
        {
            databaseFilename = "test.db";
            deleteDB(databaseFilename);
        }

        Configuration dbConfig = new Configuration.Builder(this)
                .setDatabaseName(databaseFilename)
                .setDatabaseVersion(BuildConfig.databaseVersion)
                .create();

        ActiveAndroid.initialize(dbConfig);
    }

    @Override
    public void onTerminate()
    {
        ActiveAndroid.dispose();
        super.onTerminate();
    }
}
