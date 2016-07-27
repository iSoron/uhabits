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

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

@ActivityScope
public class ListHabitsMenu extends BaseMenu
{
    @NonNull
    private final ListHabitsScreen screen;

    private final HabitCardListAdapter adapter;

    private boolean showArchived;

    private boolean showCompleted;

    private final Preferences preferences;

    @Inject
    public ListHabitsMenu(@NonNull BaseActivity activity,
                          @NonNull ListHabitsScreen screen,
                          @NonNull HabitCardListAdapter adapter,
                          @NonNull Preferences preferences)
    {
        super(activity);
        this.screen = screen;
        this.adapter = adapter;
        this.preferences = preferences;

        showCompleted = preferences.getShowCompleted();
        showArchived = preferences.getShowArchived();
        updateAdapterFilter();
    }

    @Override
    public void onCreate(@NonNull Menu menu)
    {
        MenuItem nightModeItem = menu.findItem(R.id.action_night_mode);
        nightModeItem.setChecked(InterfaceUtils.isNightMode());

        MenuItem showArchivedItem = menu.findItem(R.id.action_show_archived);
        showArchivedItem.setChecked(showArchived);

        MenuItem showCompletedItem = menu.findItem(R.id.actionShowCompleted);
        showCompletedItem.setChecked(showCompleted);
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
                toggleShowArchived();
                invalidate();
                return true;

            case R.id.actionShowCompleted:
                toggleShowCompleted();
                invalidate();
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

    private void toggleShowArchived()
    {
        showArchived = !showArchived;
        preferences.setShowArchived(showArchived);
        updateAdapterFilter();
    }

    private void toggleShowCompleted()
    {
        showCompleted = !showCompleted;
        preferences.setShowCompleted(showCompleted);
        updateAdapterFilter();
    }

    private void updateAdapterFilter()
    {
        adapter.setFilter(new HabitMatcherBuilder()
            .setArchivedAllowed(showArchived)
            .setCompletedAllowed(showCompleted)
            .build());
        adapter.refresh();
    }
}
