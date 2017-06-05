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

import android.support.annotation.*;
import android.view.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;

import javax.inject.*;

@ActivityScope
public class ListHabitsMenu extends BaseMenu
{

    @NonNull
    private final ListHabitsMenuBehavior behavior;

    private final Preferences preferences;

    private ThemeSwitcher themeSwitcher;

    @Inject
    public ListHabitsMenu(@NonNull BaseActivity activity,
                          @NonNull Preferences preferences,
                          @NonNull ThemeSwitcher themeSwitcher,
                          @NonNull ListHabitsMenuBehavior behavior)
    {
        super(activity);
        this.preferences = preferences;
        this.themeSwitcher = themeSwitcher;
        this.behavior = behavior;
    }

    @Override
    public void onCreate(@NonNull Menu menu)
    {
        MenuItem nightModeItem = menu.findItem(R.id.actionToggleNightMode);
        MenuItem hideArchivedItem = menu.findItem(R.id.actionHideArchived);
        MenuItem hideCompletedItem = menu.findItem(R.id.actionHideCompleted);
        nightModeItem.setChecked(themeSwitcher.isNightMode());
        hideArchivedItem.setChecked(!preferences.getShowArchived());
        hideCompletedItem.setChecked(!preferences.getShowCompleted());
    }

    @Override
    public boolean onItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.actionToggleNightMode:
                behavior.onToggleNightMode();
                return true;

            case R.id.actionAdd:
                behavior.onCreateHabit();
                return true;

            case R.id.actionFAQ:
                behavior.onViewFAQ();
                return true;

            case R.id.actionAbout:
                behavior.onViewAbout();
                return true;

            case R.id.actionSettings:
                behavior.onViewSettings();
                return true;

            case R.id.actionHideArchived:
                behavior.onToggleShowArchived();
                invalidate();
                return true;

            case R.id.actionHideCompleted:
                behavior.onToggleShowCompleted();
                invalidate();
                return true;

            case R.id.actionSortColor:
                behavior.onSortByColor();
                return true;

            case R.id.actionSortManual:
                behavior.onSortByManually();
                return true;

            case R.id.actionSortName:
                behavior.onSortByName();
                return true;

            case R.id.actionSortScore:
                behavior.onSortByScore();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected int getMenuResourceId()
    {
        return R.menu.list_habits;
    }

}
