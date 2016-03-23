package org.isoron.uhabits.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;

import org.isoron.uhabits.BuildConfig;

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

        SimpleDateFormat dateFormat = DateHelper.getCSVDateFormat();
        String date = dateFormat.format(DateHelper.getStartOfToday());
        File dbCopy = new File(String.format("%s/Loop-Habits-Backup-%s.db", dir.getAbsolutePath(), date));

        copy(db, dbCopy);

        return dbCopy.getAbsolutePath();
    }

    public static void deleteDatabase(Context context, String databaseFilename)
    {
        File db = getDatabaseFile(context, databaseFilename);
        if(db.exists()) db.delete();
    }

    @NonNull
    private static File getDatabaseFile(Context context, String databaseFilename)
    {
        return new File(String.format("%s/../databases/%s",
                    context.getApplicationContext().getFilesDir().getPath(), databaseFilename));
    }
}
