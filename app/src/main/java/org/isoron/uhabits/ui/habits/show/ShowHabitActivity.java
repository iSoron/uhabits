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

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.BaseActivity;

public class ShowHabitActivity extends BaseActivity
{
    private Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        habit = Habit.get(ContentUris.parseId(data));

        setContentView(R.layout.show_habit_activity);

        setupSupportActionBar(true);
        setupHabitActionBar();
    }

    private void setupHabitActionBar()
    {
        if(habit == null) return;

        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;

        actionBar.setTitle(habit.name);

        setupActionBarColor(ColorUtils.getColor(this, habit.color));
    }

    public Habit getHabit()
    {
        return habit;
    }
}
