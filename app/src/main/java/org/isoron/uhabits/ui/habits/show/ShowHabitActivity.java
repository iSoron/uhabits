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

package org.isoron.uhabits.ui.habits.show;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v7.app.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

/**
 * Activity that allows the user to see more information about a single habit.
 *
 * Shows all the metadata for the habit, in addition to several charts.
 */
public class ShowHabitActivity extends BaseActivity
{
    private Habit habit;

    @Inject
    HabitList habitList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HabitsApplication.getComponent().inject(this);

        Uri data = getIntent().getData();
        habit = habitList.getById(ContentUris.parseId(data));

        setContentView(R.layout.show_habit_activity);
        BaseScreen.setupActionBarColor(this, ColorUtils.getColor(this, habit.getColor()));

        setupHabitActionBar();
    }

    public void setupHabitActionBar()
    {
        if (habit == null) return;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(habit.getName());
    }

    public Habit getHabit()
    {
        return habit;
    }
}
