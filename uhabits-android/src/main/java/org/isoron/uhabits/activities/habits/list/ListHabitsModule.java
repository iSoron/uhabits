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

package org.isoron.uhabits.activities.habits.list;

import android.content.*;
import android.support.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;

import javax.inject.*;

import dagger.*;

class BugReporterProxy extends AndroidBugReporter
    implements ListHabitsBehavior.BugReporter
{
    @Inject
    public BugReporterProxy(@AppContext @NonNull Context context)
    {
        super(context);
    }
}

@Module
public abstract class ListHabitsModule
{
    @Binds
    abstract ListHabitsMenuBehavior.Adapter getAdapter(HabitCardListAdapter adapter);

    @Binds
    abstract ListHabitsBehavior.BugReporter getBugReporter(BugReporterProxy proxy);

    @Binds
    abstract ListHabitsMenuBehavior.Screen getMenuScreen(ListHabitsScreen screen);

    @Binds
    abstract ListHabitsBehavior.Screen getScreen(ListHabitsScreen screen);

    @Binds
    abstract ListHabitsSelectionMenuBehavior.Adapter getSelMenuAdapter(
        HabitCardListAdapter adapter);

    @Binds
    abstract ListHabitsSelectionMenuBehavior.Screen getSelMenuScreen(
        ListHabitsScreen screen);

    @Binds
    abstract ListHabitsBehavior.DirFinder getSystem(HabitsDirFinder system);
}
