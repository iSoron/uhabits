/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.screens.habits.list;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;

import javax.inject.*;

public class ListHabitsMenuBehavior
{
    @NonNull
    private final Screen screen;

    @NonNull
    private final Adapter adapter;

    @NonNull
    private final Preferences preferences;

    @NonNull
    private final ThemeSwitcher themeSwitcher;

    private boolean showCompleted;

    private boolean showArchived;

    @Inject
    public ListHabitsMenuBehavior(@NonNull Screen screen,
                                  @NonNull Adapter adapter,
                                  @NonNull Preferences preferences,
                                  @NonNull ThemeSwitcher themeSwitcher)
    {
        this.screen = screen;
        this.adapter = adapter;
        this.preferences = preferences;
        this.themeSwitcher = themeSwitcher;

        showCompleted = preferences.getShowCompleted();
        showArchived = preferences.getShowArchived();
        updateAdapterFilter();
    }

    public void onCreateHabit()
    {
        screen.showSelectHabitTypeDialog();
    }

    public void onViewFAQ()
    {
        screen.showFAQScreen();
    }

    public void onViewAbout()
    {
        screen.showAboutScreen();
    }

    public void onViewSettings()
    {
        screen.showSettingsScreen();
    }

    public void onToggleShowArchived()
    {
        showArchived = !showArchived;
        preferences.setShowArchived(showArchived);
        updateAdapterFilter();
    }

    public void onToggleShowCompleted()
    {
        showCompleted = !showCompleted;
        preferences.setShowCompleted(showCompleted);
        updateAdapterFilter();
    }

    public void onSortByManually()
    {
        adapter.setOrder(HabitList.Order.BY_POSITION);
    }

    public void onSortByColor()
    {
        onSortToggleBy(HabitList.Order.BY_COLOR_ASC, HabitList.Order.BY_COLOR_DESC);
    }


    public void onSortByScore()
    {
        onSortToggleBy(HabitList.Order.BY_SCORE_DESC, HabitList.Order.BY_SCORE_ASC);
    }

    public void onSortByName()
    {
        onSortToggleBy(HabitList.Order.BY_NAME_ASC, HabitList.Order.BY_NAME_DESC);
    }

    public void onSortByStatus()
    {
        onSortToggleBy(HabitList.Order.BY_STATUS_ASC, HabitList.Order.BY_STATUS_DESC);
    }

    private void onSortToggleBy(HabitList.Order defaultOrder, HabitList.Order reversedOrder)
    {
        if (adapter.getOrder() != defaultOrder) {
            if (adapter.getOrder() != reversedOrder) {
                adapter.setPreviousOrder(adapter.getOrder());
            }
            adapter.setOrder(defaultOrder);
        } else {
            adapter.setOrder(reversedOrder);
        }

    }

    public void onToggleNightMode()
    {
        themeSwitcher.toggleNightMode();
        screen.applyTheme();
    }

    private void updateAdapterFilter()
    {
        adapter.setFilter(new HabitMatcherBuilder()
            .setArchivedAllowed(showArchived)
            .setCompletedAllowed(showCompleted)
            .build());
        adapter.refresh();
    }

    public interface Adapter
    {
        void refresh();

        void setFilter(HabitMatcher build);

        void setOrder(HabitList.Order order);

        void setPreviousOrder(HabitList.Order order);

        HabitList.Order getOrder();
    }

    public interface Screen
    {
        void applyTheme();

        void showAboutScreen();

        void showFAQScreen();

        void showSettingsScreen();

        void showSelectHabitTypeDialog();
    }
}
