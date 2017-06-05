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

package org.isoron.uhabits.activities.habits.list;

import android.os.*;

import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.utils.*;

/**
 * Activity that allows the user to see and modify the list of habits.
 */
public class ListHabitsActivity extends HabitsActivity
{
    private HabitCardListAdapter adapter;

    private ListHabitsRootView rootView;

    private ListHabitsScreen screen;

    private boolean pureBlack;

    private Preferences prefs;

    private MidnightTimer midnightTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        midnightTimer = getAppComponent().getMidnightTimer();
        HabitsActivityComponent component = getActivityComponent();

        ListHabitsMenu menu = component.getListHabitsMenu();
        ListHabitsSelectionMenu selectionMenu = component.getListHabitsSelectionMenu();
        ListHabitsController controller = component.getListHabitsController();

        adapter = component.getHabitCardListAdapter();
        rootView = component.getListHabitsRootView();
        screen = component.getListHabitsScreen();

        prefs = getAppComponent().getPreferences();
        pureBlack = prefs.isPureBlackEnabled();

        screen.setMenu(menu);
        screen.setController(controller);
        screen.setSelectionMenu(selectionMenu);
        rootView.setController(controller, selectionMenu);

//        if(prefs.isSyncEnabled())
//            startService(new Intent(this, SyncService.class));

        setScreen(screen);
        controller.onStartup();
    }

    @Override
    protected void onPause()
    {
        midnightTimer.onPause();
        screen.onDettached();
        adapter.cancelRefresh();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        adapter.refresh();
        screen.onAttached();
        rootView.postInvalidate();
        midnightTimer.onResume();

        if (prefs.getTheme() == ThemeSwitcher.THEME_DARK &&
            prefs.isPureBlackEnabled() != pureBlack)
        {
            restartWithFade(ListHabitsActivity.class);
        }

        super.onResume();
    }
}
