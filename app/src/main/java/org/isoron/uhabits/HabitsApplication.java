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
import android.support.annotation.Nullable;

import com.activeandroid.ActiveAndroid;

import org.isoron.uhabits.helpers.DatabaseHelper;

import java.io.File;

public class HabitsApplication extends Application
{
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

    @Override
    public void onCreate()
    {
        super.onCreate();
        HabitsApplication.context = this;

        if (isTestMode())
        {
            File db = DatabaseHelper.getDatabaseFile();
            if(db.exists()) db.delete();
        }

        DatabaseHelper.initializeActiveAndroid();
    }

    @Override
    public void onTerminate()
    {
        HabitsApplication.context = null;
        ActiveAndroid.dispose();
        super.onTerminate();
    }
}
