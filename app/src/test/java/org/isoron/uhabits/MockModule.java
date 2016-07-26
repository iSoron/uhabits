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

import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.common.dialogs.*;
import org.isoron.uhabits.ui.widgets.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

import dagger.*;

import static org.mockito.Mockito.*;

@Module
public class MockModule
{
    @Provides
    @Singleton
    DialogFactory provideDialogFactory()
    {
        return mock(DialogFactory.class);
    }

    @Provides
    @Singleton
    DirFinder provideDirFinder()
    {
        return mock(DirFinder.class);
    }

    @Provides
    Habit provideHabit()
    {
        return mock(Habit.class);
    }

    @Provides
    @Singleton
    IntentFactory provideIntentFactory()
    {
        return mock(IntentFactory.class);
    }

    @Provides
    @Singleton
    IntentScheduler provideIntentScheduler()
    {
        return mock(IntentScheduler.class);
    }

    @Provides
    @Singleton
    HabitLogger provideLogger()
    {
        return mock(HabitLogger.class);
    }

    @Singleton
    @Provides
    PendingIntentFactory providePendingIntentFactory()
    {
        return mock(PendingIntentFactory.class);
    }

    @Singleton
    @Provides
    Preferences providePreferences()
    {
        return mock(Preferences.class);
    }

    @Provides
    @Singleton
    ReminderScheduler provideReminderScheduler()
    {
        return mock(ReminderScheduler.class);
    }

    @Provides
    @Singleton
    WidgetPreferences provideWidgetPreferences()
    {
        return mock(WidgetPreferences.class);
    }

    @Provides
    @Singleton
    TaskRunner provideTaskRunner()
    {
        return new SingleThreadTaskRunner();
    }

    @Provides
    @Singleton
    WidgetUpdater provideWidgetUpdate()
    {
        return mock(WidgetUpdater.class);
    }
}
