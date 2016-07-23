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

import android.content.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.*;
import org.isoron.uhabits.tasks.*;

import javax.inject.*;

import dagger.*;

/**
 * Module that provides dependencies when the application is running on
 * Android.
 * <p>
 * This module is also used for instrumented tests.
 */
@Module
public class AndroidModule
{
    @Provides
    @Singleton
    static HabitList provideHabitList()
    {
        return SQLiteHabitList.getInstance();
    }

    @Provides
    static ModelFactory provideModelFactory()
    {
        return new SQLModelFactory();
    }

    @Provides
    @Singleton
    static Context provideApplicationContext()
    {
        return HabitsApplication.getContext();
    }

    @Provides
    @Singleton
    static TaskRunner provideTaskRunner()
    {
        return new AndroidTaskRunner();
    }
}
