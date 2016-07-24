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

import android.os.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.habits.list.model.*;

/**
 * Activity that allows the user to see and modify the list of habits.
 */
public class ListHabitsActivity extends BaseActivity
{
    private HabitList habits;

    private HabitCardListAdapter adapter;

    private ListHabitsRootView rootView;

    private ListHabitsScreen screen;

    private ListHabitsMenu menu;

    private ListHabitsSelectionMenu selectionMenu;

    private ListHabitsController controller;

    private BaseSystem system;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        habits = HabitsApplication.getComponent().getHabitList();

        int checkmarkCount = ListHabitsRootView.MAX_CHECKMARK_COUNT;

        system = new BaseSystem(this);
        adapter = new HabitCardListAdapter(habits, checkmarkCount);

        rootView = new ListHabitsRootView(this, adapter);
        screen = new ListHabitsScreen(this, rootView);
        menu = new ListHabitsMenu(this, screen, adapter);
        selectionMenu = new ListHabitsSelectionMenu(habits, screen, adapter);
        controller = new ListHabitsController(habits, screen, system, adapter);

        screen.setMenu(menu);
        screen.setController(controller);
        screen.setSelectionMenu(selectionMenu);
        rootView.setController(controller, selectionMenu);

        setScreen(screen);
        controller.onStartup();
    }

    @Override
    protected void onPause()
    {
        adapter.cancelRefresh();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        adapter.refresh();
        rootView.postInvalidate();
        super.onResume();
    }
}
