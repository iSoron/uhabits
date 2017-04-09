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

import android.content.*;
import android.support.annotation.*;

import com.activeandroid.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.sqlite.records.*;

import java.io.*;
import java.text.*;

public abstract class DatabaseUtils
{
    public static void executeAsTransaction(Callback callback)
    {
        ActiveAndroid.beginTransaction();
        try
        {
            callback.execute();
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    @NonNull
    public static File getDatabaseFile(Context context)
    {
        String databaseFilename = getDatabaseFilename();
        String root = context.getFilesDir().getPath();

        String format = "%s/../databases/%s";
        String filename = String.format(format, root, databaseFilename);

        return new File(filename);
    }

    @NonNull
    public static String getDatabaseFilename()
    {
        String databaseFilename = BuildConfig.databaseFilename;
        if (HabitsApplication.isTestMode()) databaseFilename = "test.db";
        return databaseFilename;
    }

    @SuppressWarnings("unchecked")
    public static void initializeActiveAndroid(Context context)
    {
        Configuration dbConfig = new Configuration.Builder(context)
            .setDatabaseName(getDatabaseFilename())
            .setDatabaseVersion(BuildConfig.databaseVersion)
            .addModelClasses(CheckmarkRecord.class, HabitRecord.class,
                RepetitionRecord.class, ScoreRecord.class, StreakRecord.class)
            .create();

        ActiveAndroid.initialize(dbConfig);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String saveDatabaseCopy(Context context, File dir) throws IOException
    {
        SimpleDateFormat dateFormat = DateFormats.getBackupDateFormat();
        String date = dateFormat.format(DateUtils.getLocalTime());
        String format = "%s/Loop Habits Backup %s.db";
        String filename = String.format(format, dir.getAbsolutePath(), date);

        File db = getDatabaseFile(context);
        File dbCopy = new File(filename);
        FileUtils.copy(db, dbCopy);

        return dbCopy.getAbsolutePath();
    }

    public interface Callback
    {
        void execute();
    }
}
