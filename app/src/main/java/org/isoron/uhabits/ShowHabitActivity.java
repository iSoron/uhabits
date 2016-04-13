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

import android.content.ContentUris;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;
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

        if (!UIHelper.getStyledBoolean(this, R.attr.useHabitColorAsPrimary)) return;

        int color = ColorHelper.getColor(this, habit.color);
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int darkerColor = ColorHelper.mixColors(color, Color.BLACK, 0.75f);
            getWindow().setStatusBarColor(darkerColor);
        }
    }

    public Habit getHabit()
    {
        return habit;
    }
}
