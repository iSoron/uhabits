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

package org.isoron.uhabits.activities.habits.show;

import android.support.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.show.*;

import dagger.*;

@Module
public class ShowHabitModule extends ActivityModule
{
    private Habit habit;

    public ShowHabitModule(@NonNull BaseActivity activity, @NonNull Habit habit)
    {
        super(activity);
        this.habit = habit;
    }

    @Provides
    public Habit getHabit()
    {
        return habit;
    }

    @Provides
    public ShowHabitBehavior.Screen getScreen(ShowHabitScreen screen)
    {
        return screen;
    }

    @Provides
    public ShowHabitMenuBehavior.Screen getMenuScreen(ShowHabitScreen screen)
    {
        return screen;
    }

    @Provides
    public ShowHabitMenuBehavior.System getSystem(BaseSystem system)
    {
        return system;
    }
}
