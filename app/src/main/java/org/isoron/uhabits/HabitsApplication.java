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

import android.app.*;
import android.content.*;
import android.support.annotation.*;

import com.activeandroid.*;

import org.isoron.uhabits.notifications.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.*;

import java.io.*;

/**
 * The Android application for Loop Habit Tracker.
 */
public class HabitsApplication extends Application
{
    private Context context;

    private static AppComponent component;

    private WidgetUpdater widgetUpdater;

    private ReminderScheduler reminderScheduler;

    private NotificationTray notificationTray;

    public AppComponent getComponent()
    {
        return component;
    }

    public static void setComponent(AppComponent component)
    {
        HabitsApplication.component = component;
    }

    public static boolean isTestMode()
    {
        try
        {
            Class.forName ("org.isoron.uhabits.BaseAndroidTest");
            return true;
        }
        catch (final ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;

        component = DaggerAppComponent
            .builder()
            .appModule(new AppModule(context))
            .build();

        if (isTestMode())
        {
            File db = DatabaseUtils.getDatabaseFile(context);
            if (db.exists()) db.delete();
        }

        DatabaseUtils.initializeActiveAndroid(context);

        widgetUpdater = component.getWidgetUpdater();
        widgetUpdater.startListening();

        reminderScheduler = component.getReminderScheduler();
        reminderScheduler.startListening();

        notificationTray = component.getNotificationTray();
        notificationTray.startListening();

        Preferences prefs = component.getPreferences();
        prefs.initialize();
        prefs.updateLastAppVersion();

        TaskRunner taskRunner = component.getTaskRunner();
        taskRunner.execute(() -> {
            reminderScheduler.scheduleAll();
            widgetUpdater.updateWidgets();
        });
    }

    @Override
    public void onTerminate()
    {
        context = null;
        ActiveAndroid.dispose();

        reminderScheduler.stopListening();
        widgetUpdater.stopListening();
        notificationTray.stopListening();
        super.onTerminate();
    }
}
