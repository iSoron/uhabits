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

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.uhabits.R;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.BaseMenu;
import org.isoron.uhabits.utils.InterfaceUtils;

public class ListHabitsMenu extends BaseMenu
{
    @NonNull
    private final ListHabitsScreen screen;

    private boolean showArchived;

    public ListHabitsMenu(@NonNull BaseActivity activity,
                          @NonNull ListHabitsScreen screen)
    {
        super(activity);
        this.screen = screen;
    }

    @Override
    public void onCreate(@NonNull Menu menu)
    {
        MenuItem nightModeItem = menu.findItem(R.id.action_night_mode);
        nightModeItem.setChecked(InterfaceUtils.isNightMode());

        MenuItem showArchivedItem = menu.findItem(R.id.action_show_archived);
        showArchivedItem.setChecked(showArchived);
    }

    @Override
    public boolean onItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_night_mode:
                screen.toggleNightMode();
                return true;

            case R.id.action_add:
                screen.showCreateHabitScreen();
                return true;

            case R.id.action_faq:
                screen.showFAQScreen();
                return true;

            case R.id.action_about:
                screen.showAboutScreen();
                return true;

            case R.id.action_settings:
                screen.showSettingsScreen();
                return true;

            case R.id.action_show_archived:
                showArchived = !showArchived;
                screen.getRootView().setShowArchived(showArchived);
                invalidate();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected int getMenuResourceId()
    {
        return R.menu.main_activity;
    }
}
