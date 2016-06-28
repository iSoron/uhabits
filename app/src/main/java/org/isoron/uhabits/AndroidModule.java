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

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.*;
import org.isoron.uhabits.utils.*;

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
    CommandRunner provideCommandRunner()
    {
        return new CommandRunner();
    }

    @Provides
    @Singleton
    HabitList provideHabitList()
    {
        return SQLiteHabitList.getInstance();
    }

    @Provides
    ModelFactory provideModelFactory()
    {
        return new SQLModelFactory();
    }

    @Provides
    @Singleton
    Preferences providePreferences()
    {
        return new Preferences();
    }

    @Provides
    @Singleton
    WidgetPreferences provideWidgetPreferences()
    {
        return new WidgetPreferences();
    }
}
