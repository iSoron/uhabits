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

package org.isoron.uhabits.activities.habits.show;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.models.*;

/**
 * Activity that allows the user to see more information about a single habit.
 * <p>
 * Shows all the metadata for the habit, in addition to several charts.
 */
public class ShowHabitActivity extends BaseActivity
{
    private HabitList habits;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        HabitsApplication app = (HabitsApplication) getApplicationContext();
        habits = app.getComponent().getHabitList();
        Habit habit = getHabitFromIntent();

        ShowHabitComponent component = DaggerShowHabitComponent
            .builder()
            .appComponent(app.getComponent())
            .showHabitModule(new ShowHabitModule(this, habit))
            .build();

        ShowHabitRootView rootView = component.getRootView();
        ShowHabitScreen screen = component.getScreen();

        setScreen(screen);
        screen.setMenu(component.getMenu());
        screen.setController(component.getController());
        rootView.setController(component.getController());

        screen.reattachDialogs();
    }

    @NonNull
    private Habit getHabitFromIntent()
    {
        Uri data = getIntent().getData();
        Habit habit = habits.getById(ContentUris.parseId(data));
        if (habit == null) throw new RuntimeException("habit not found");
        return habit;
    }
}
