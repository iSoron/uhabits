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

import org.isoron.uhabits.commands.CommandRunner;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.HabitList;
import org.isoron.uhabits.models.ModelFactory;
import org.isoron.uhabits.models.memory.MemoryHabitList;
import org.isoron.uhabits.models.memory.MemoryModelFactory;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListCache;
import org.isoron.uhabits.utils.Preferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public class TestModule
{
    @Singleton
    @Provides
    Preferences providePreferences()
    {
        return mock(Preferences.class);
    }

    @Singleton
    @Provides
    CommandRunner provideCommandRunner()
    {
        return mock(CommandRunner.class);
    }

    @Singleton
    @Provides
    HabitCardListCache provideHabitCardListCache()
    {
        return mock(HabitCardListCache.class);
    }

    @Singleton
    @Provides
    HabitList provideHabitList()
    {
        return new MemoryHabitList();
    }

    @Provides
    Habit provideHabit()
    {
        return mock(Habit.class);
    }

    @Provides
    @Singleton
    ModelFactory provideModelFactory()
    {
        return new MemoryModelFactory();
    }
}
