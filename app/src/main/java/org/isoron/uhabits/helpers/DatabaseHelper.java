package org.isoron.uhabits.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Repetition;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.models.Streak;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

public class DatabaseHelper
{
    public static void copy(File src, File dst) throws IOException
    {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

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
    public static String saveDatabaseCopy(Context context, File dir) throws IOException
    {
        File db = getDatabaseFile(context, BuildConfig.databaseFilename);

        SimpleDateFormat dateFormat = DateHelper.getBackupDateFormat();
        String date = dateFormat.format(DateHelper.getLocalTime());
        File dbCopy = new File(String.format("%s/Loop Habits Backup %s.db", dir.getAbsolutePath(), date));

        copy(db, dbCopy);

        return dbCopy.getAbsolutePath();
    }

    public static void deleteDatabase(Context context, String databaseFilename)
    {
        File db = getDatabaseFile(context, databaseFilename);
        if(db.exists()) db.delete();
    }

    @NonNull
    public static File getDatabaseFile(Context context, String databaseFilename)
    {
        return new File(String.format("%s/../databases/%s",
                    context.getApplicationContext().getFilesDir().getPath(), databaseFilename));
    }

    @Nullable
    public static File getFilesDir(Context context, String prefix)
    {
        File baseDir = context.getExternalFilesDir(null);
        if(baseDir == null) return null;
        if(!baseDir.canWrite()) return null;

        File dir = new File(String.format("%s/%s/", baseDir.getAbsolutePath(), prefix));
        dir.mkdirs();
        return dir;
    }

    @SuppressWarnings("unchecked")
    public static void initializeActiveAndroid(Context context, String databaseFilename)
    {
        Configuration dbConfig = new Configuration.Builder(context)
                .setDatabaseName(databaseFilename)
                .setDatabaseVersion(BuildConfig.databaseVersion)
                .addModelClasses(Checkmark.class, Habit.class, Repetition.class, Score.class,
                        Streak.class)
                .create();

        ActiveAndroid.initialize(dbConfig);
    }
}
