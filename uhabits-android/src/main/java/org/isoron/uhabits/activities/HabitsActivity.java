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

package org.isoron.uhabits.activities;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;

public abstract class HabitsActivity extends BaseActivity
{
    private HabitsActivityComponent component;

    private HabitsApplicationComponent appComponent;

    public HabitsActivityComponent getActivityComponent()
    {
        return component;
    }

    public HabitsApplicationComponent getAppComponent()
    {
        return appComponent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        appComponent =
            ((HabitsApplication) getApplicationContext()).getComponent();

        Habit habit = getHabitFromIntent(appComponent.getHabitList());

        component = DaggerHabitsActivityComponent
            .builder()
            .activityModule(new ActivityModule(this))
            .habitModule(new HabitModule(habit))
            .habitsApplicationComponent(appComponent)
            .build();

        component.getThemeSwitcher().apply();
    }

    @Nullable
    private Habit getHabitFromIntent(@NonNull HabitList habitList)
    {
        Uri data = getIntent().getData();
        if(data == null) return null;

        Habit habit = habitList.getById(ContentUris.parseId(data));
        if (habit == null) throw new RuntimeException("habit not found");

        return habit;
    }
}
