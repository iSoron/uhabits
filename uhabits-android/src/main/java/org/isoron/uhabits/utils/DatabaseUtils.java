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
import android.database.sqlite.*;
import android.support.annotation.*;

import org.isoron.androidbase.storage.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.models.sqlite.*;

import java.io.*;
import java.text.*;

public abstract class DatabaseUtils
{
    @Nullable
    private static BaseSQLiteOpenHelper helper = null;

    public static void executeAsTransaction(Callback callback)
    {
        try (SQLiteDatabase db = openDatabase())
        {
            db.beginTransaction();
            try
            {
                callback.execute();
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                db.endTransaction();
            }
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
    public static void initializeDatabase(Context context)
    {
        try
        {
            helper = new BaseSQLiteOpenHelper(context, getDatabaseFilename(),
                BuildConfig.databaseVersion);
        }
        catch (RuntimeException e)
        {
            if (e.getMessage().contains("downgrade"))
                throw new InvalidDatabaseVersionException();
            else throw e;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String saveDatabaseCopy(Context context, File dir)
        throws IOException
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

    @NonNull
    public static SQLiteDatabase openDatabase()
    {
        if(helper == null) throw new IllegalStateException();
        return helper.getWritableDatabase();
    }

    public static void dispose()
    {
        helper = null;
    }

    public interface Callback
    {
        void execute() throws Exception;
    }
}
