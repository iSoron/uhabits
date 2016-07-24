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

import org.isoron.uhabits.ui.widgets.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

/**
 * The Android application for Loop Habit Tracker.
 */
public class HabitsApplication extends Application
{
    public static final int RESULT_BUG_REPORT = 4;

    public static final int RESULT_EXPORT_CSV = 2;

    public static final int RESULT_EXPORT_DB = 3;

    public static final int RESULT_IMPORT_DATA = 1;

    @Nullable
    private static HabitsApplication application;

    private static BaseComponent component;

    @Nullable
    private static Context context;

    private static WidgetUpdater widgetUpdater;

    public static BaseComponent getComponent()
    {
        return component;
    }

    public static void setComponent(BaseComponent component)
    {
        HabitsApplication.component = component;
    }

    @NonNull
    public static Context getContext()
    {
        if (context == null) throw new RuntimeException("context is null");
        return context;
    }

    @Nullable
    public static HabitsApplication getInstance()
    {
        return application;
    }

    @NonNull
    public static WidgetUpdater getWidgetUpdater()
    {
        if (widgetUpdater == null) throw new RuntimeException(
                "widgetUpdater is null");

        return widgetUpdater;
    }

    public static boolean isTestMode()
    {
        try
        {
            if (context != null)
            {
                String testClass = "org.isoron.uhabits.BaseAndroidTest";
                context.getClassLoader().loadClass(testClass);
            }
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        HabitsApplication.context = this;
        HabitsApplication.application = this;
        component = DaggerAndroidComponent.builder().build();

        if (isTestMode())
        {
            File db = DatabaseUtils.getDatabaseFile();
            if (db.exists()) db.delete();
        }

        widgetUpdater = new WidgetUpdater(this);
        widgetUpdater.startListening();

        DatabaseUtils.initializeActiveAndroid();
    }

    @Override
    public void onTerminate()
    {
        HabitsApplication.context = null;
        ActiveAndroid.dispose();
        widgetUpdater.stopListening();
        super.onTerminate();
    }
}
