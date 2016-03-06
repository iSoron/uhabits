/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.content.ContentUris;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.models.Habit;

public class ShowHabitActivity extends ReplayableActivity
{

    public Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        habit = Habit.get(ContentUris.parseId(data));
        getActionBar().setTitle(habit.name);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            getActionBar().setBackgroundDrawable(new ColorDrawable(habit.color));
        }

        setContentView(R.layout.show_habit_activity);
    }
}
