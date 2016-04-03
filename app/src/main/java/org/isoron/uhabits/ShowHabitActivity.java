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

import android.app.ActionBar;
import android.content.ContentUris;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import org.isoron.uhabits.models.Habit;

public class ShowHabitActivity extends BaseActivity
{
    private Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        habit = Habit.get(ContentUris.parseId(data));
        ActionBar actionBar = getActionBar();

        if(actionBar != null && getHabit() != null)
        {
            actionBar.setTitle(getHabit().name);
            if (android.os.Build.VERSION.SDK_INT >= 21)
                actionBar.setBackgroundDrawable(new ColorDrawable(getHabit().color));
        }

        setContentView(R.layout.show_habit_activity);
    }

    public Habit getHabit()
    {
        return habit;
    }
}
