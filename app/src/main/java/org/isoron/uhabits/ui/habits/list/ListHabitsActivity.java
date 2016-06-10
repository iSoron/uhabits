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

package org.isoron.uhabits.ui.habits.list;

import android.os.Bundle;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.models.HabitList;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.BaseSystem;

import javax.inject.Inject;

/**
 * Activity that allows the user to see and modify the list of habits.
 */
public class ListHabitsActivity extends BaseActivity
{
    @Inject
    HabitList habitList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HabitsApplication.getComponent().inject(this);

        BaseSystem system = new BaseSystem(this);
        ListHabitsScreen screen = new ListHabitsScreen(this);
        ListHabitsController controller =
            new ListHabitsController(screen, system, habitList);

        screen.setController(controller);
        setScreen(screen);
        controller.onStartup();
    }
}
