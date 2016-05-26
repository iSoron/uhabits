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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.activeandroid.ActiveAndroid;

import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.utils.DatabaseUtils;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.FileUtils;
import org.isoron.uhabits.utils.ReminderUtils;
import org.isoron.uhabits.widgets.WidgetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class HabitsApplication extends Application implements MainController.System
{
    public static final String ACTION_REFRESH = "org.isoron.uhabits.ACTION_REFRESH";
    public static final int RESULT_IMPORT_DATA = 1;
    public static final int RESULT_EXPORT_CSV = 2;
    public static final int RESULT_EXPORT_DB = 3;
    public static final int RESULT_BUG_REPORT = 4;

    @Nullable
    private static HabitsApplication application;

    @Nullable
    private static Context context;

    public static boolean isTestMode()
    {
        try
        {
            if(context != null)
                context.getClassLoader().loadClass("org.isoron.uhabits.unit.models.HabitTest");
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Nullable
    public static Context getContext()
    {
        return context;
    }

    @Nullable
    public static HabitsApplication getInstance()
    {
        return application;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        HabitsApplication.context = this;
        HabitsApplication.application = this;

        if (isTestMode())
        {
            File db = DatabaseUtils.getDatabaseFile();
            if(db.exists()) db.delete();
        }

        DatabaseUtils.initializeActiveAndroid();
    }

    @Override
    public void onTerminate()
    {
        HabitsApplication.context = null;
        ActiveAndroid.dispose();
        super.onTerminate();
    }

    public String getLogcat() throws IOException
    {
        int maxNLines = 250;
        StringBuilder builder = new StringBuilder();

        String[] command = new String[] { "logcat", "-d"};
        Process process = Runtime.getRuntime().exec(command);

        InputStreamReader in = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(in);

        LinkedList<String> log = new LinkedList<>();

        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            log.addLast(line);
            if(log.size() > maxNLines) log.removeFirst();
        }

        for(String l : log)
        {
            builder.append(l);
            builder.append('\n');
        }

        return builder.toString();
    }

    public String getDeviceInfo()
    {
        if(context == null) return "";

        StringBuilder b = new StringBuilder();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        b.append(String.format("App Version Name: %s\n", BuildConfig.VERSION_NAME));
        b.append(String.format("App Version Code: %s\n", BuildConfig.VERSION_CODE));
        b.append(String.format("OS Version: %s (%s)\n", java.lang.System.getProperty("os.version"),
                android.os.Build.VERSION.INCREMENTAL));
        b.append(String.format("OS API Level: %s\n", android.os.Build.VERSION.SDK));
        b.append(String.format("Device: %s\n", android.os.Build.DEVICE));
        b.append(String.format("Model (Product): %s (%s)\n", android.os.Build.MODEL,
                android.os.Build.PRODUCT));
        b.append(String.format("Manufacturer: %s\n", android.os.Build.MANUFACTURER));
        b.append(String.format("Other tags: %s\n", android.os.Build.TAGS));
        b.append(String.format("Screen Width: %s\n", wm.getDefaultDisplay().getWidth()));
        b.append(String.format("Screen Height: %s\n", wm.getDefaultDisplay().getHeight()));
        b.append(String.format("SD Card state: %s\n\n", Environment.getExternalStorageState()));

        return b.toString();
    }

    @NonNull
    public File dumpBugReportToFile() throws IOException
    {
        String date = DateUtils.getBackupDateFormat().format(DateUtils.getLocalTime());

        if(context == null) throw new RuntimeException("application context should not be null");
        File dir = FileUtils.getFilesDir("Logs");
        if (dir == null) throw new IOException("log dir should not be null");

        File logFile = new File(String.format("%s/Log %s.txt", dir.getPath(), date));
        FileWriter output = new FileWriter(logFile);
        output.write(getBugReport());
        output.close();

        return logFile;
    }

    @NonNull
    public String getBugReport() throws IOException
    {
        String logcat = getLogcat();
        String deviceInfo = getDeviceInfo();
        return deviceInfo + "\n" + logcat;
    }

    public void sendFile(@NonNull String archiveFilename)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(archiveFilename)));
        startActivity(intent);
    }

    public void sendEmail(String to, String subject, String content)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(intent);
    }

    public void scheduleReminders()
    {
        new BaseTask()
        {

            @Override
            protected void doInBackground()
            {
                ReminderUtils.createReminderAlarms(getContext());
            }
        }.execute();
    }

    public void updateWidgets()
    {
        new BaseTask()
        {

            @Override
            protected void doInBackground()
            {
                WidgetManager.updateWidgets(getContext());
            }
        }.execute();
    }
}
