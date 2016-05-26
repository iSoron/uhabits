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

package org.isoron.uhabits.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Configuration;

import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Repetition;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.models.Streak;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public abstract class DatabaseUtils
{
    public interface Command
    {
        void execute();
    }

    public static void executeAsTransaction(Command command)
    {
        ActiveAndroid.beginTransaction();
        try
        {
            command.execute();
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String saveDatabaseCopy(File dir) throws IOException
    {
        File db = getDatabaseFile();

        SimpleDateFormat dateFormat = DateUtils.getBackupDateFormat();
        String date = dateFormat.format(DateUtils.getLocalTime());
        File dbCopy = new File(String.format("%s/Loop Habits Backup %s.db", dir.getAbsolutePath(), date));

        FileUtils.copy(db, dbCopy);

        return dbCopy.getAbsolutePath();
    }

    @NonNull
    public static File getDatabaseFile()
    {
        Context context = HabitsApplication.getContext();
        if(context == null) throw new RuntimeException("No application context found");

        String databaseFilename = getDatabaseFilename();

        return new File(String.format("%s/../databases/%s",
                    context.getApplicationContext().getFilesDir().getPath(), databaseFilename));
    }

    @NonNull
    public static String getDatabaseFilename()
    {
        String databaseFilename = BuildConfig.databaseFilename;

        if (HabitsApplication.isTestMode())
            databaseFilename = "test.db";

        return databaseFilename;
    }

    @SuppressWarnings("unchecked")
    public static void initializeActiveAndroid()
    {
        Context context = HabitsApplication.getContext();
        if(context == null) throw new RuntimeException("application context should not be null");

        Configuration dbConfig = new Configuration.Builder(context)
                .setDatabaseName(getDatabaseFilename())
                .setDatabaseVersion(BuildConfig.databaseVersion)
                .addModelClasses(Checkmark.class, Habit.class, Repetition.class, Score.class,
                        Streak.class)
                .create();

        ActiveAndroid.initialize(dbConfig);
    }

    public static long longQuery(String query, String args[])
    {
        Cursor c = null;

        try
        {
            c = Cache.openDatabase().rawQuery(query, args);
            if (!c.moveToFirst()) return 0;
            return c.getLong(0);
        }
        finally
        {
            if(c != null) c.close();
        }
    }
}
