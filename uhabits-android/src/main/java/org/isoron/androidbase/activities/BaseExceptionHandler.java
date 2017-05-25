/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.androidbase.activities;

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.*;

public class BaseExceptionHandler implements Thread.UncaughtExceptionHandler
{
    @Nullable
    private Thread.UncaughtExceptionHandler originalHandler;

    @NonNull
    private BaseActivity activity;

    public BaseExceptionHandler(@NonNull BaseActivity activity)
    {
        this.activity = activity;
        originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@Nullable Thread thread,
                                  @Nullable Throwable ex)
    {
        if (ex == null) return;

        try
        {
            ex.printStackTrace();
            new BaseSystem(activity).dumpBugReportToFile();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (ex.getCause() instanceof InconsistentDatabaseException)
        {
            HabitsApplication app = (HabitsApplication) activity.getApplication();
            HabitList habits = app.getComponent().getHabitList();
            habits.repair();
            System.exit(0);
        }

        if (originalHandler != null)
            originalHandler.uncaughtException(thread, ex);
    }
}
