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

package org.isoron.uhabits;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.reminders.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.database.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.notifications.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;

import dagger.*;

@Module
public class HabitsModule
{
    @Provides
    @AppScope
    public static Preferences getPreferences(SharedPreferencesStorage storage)
    {
        return new Preferences(storage);
    }

    @Provides
    @AppScope
    public static ReminderScheduler getReminderScheduler(IntentScheduler sys,
                                                         CommandRunner commandRunner,
                                                         HabitList habitList)
    {
        return new ReminderScheduler(commandRunner, habitList, sys);
    }

    @Provides
    @AppScope
    public static NotificationTray getTray(TaskRunner taskRunner,
                                           CommandRunner commandRunner,
                                           Preferences preferences,
                                           AndroidNotificationTray screen)
    {
        return new NotificationTray(taskRunner, commandRunner, preferences,
            screen);
    }

    @Provides
    @AppScope
    public static WidgetPreferences getWidgetPreferences(
        SharedPreferencesStorage storage)
    {
        return new WidgetPreferences(storage);
    }

    @Provides
    public ModelFactory getModelFactory()
    {
        return new SQLModelFactory(
            new AndroidDatabase(DatabaseUtils.openDatabase()));
    }

    @Provides
    @AppScope
    public HabitList getHabitList(SQLiteHabitList list)
    {
        return list;
    }

    @Provides
    @AppScope
    public DatabaseOpener getDatabaseOpener(AndroidDatabaseOpener opener)
    {
        return opener;
    }
}

