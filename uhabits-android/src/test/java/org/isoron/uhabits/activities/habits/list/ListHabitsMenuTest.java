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

package org.isoron.uhabits.activities.habits.list;

import android.view.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.isoron.uhabits.preferences.*;
import org.junit.*;

import static org.mockito.Mockito.*;

public class ListHabitsMenuTest extends BaseAndroidUnitTest
{
    private BaseActivity activity;

    private AndroidPreferences preferences;

    private ThemeSwitcher themeSwitcher;

    private ListHabitsMenu menu;

    private ListHabitsMenuBehavior behavior;

    @Before
    @Override
    public void setUp()
    {
        activity = mock(BaseActivity.class);
        preferences = mock(AndroidPreferences.class);
        themeSwitcher = mock(ThemeSwitcher.class);
        behavior = mock(ListHabitsMenuBehavior.class);

        menu = new ListHabitsMenu(activity, preferences,
            themeSwitcher, behavior);
    }

    @Test
    public void testOnCreate()
    {
        MenuItem nightModeItem = mock(MenuItem.class);
        MenuItem hideArchivedItem = mock(MenuItem.class);
        MenuItem hideCompletedItem = mock(MenuItem.class);
        Menu androidMenu = mock(Menu.class);
        when(androidMenu.findItem(R.id.actionToggleNightMode)).thenReturn(
            nightModeItem);
        when(androidMenu.findItem(R.id.actionHideArchived)).thenReturn(
            hideArchivedItem);
        when(androidMenu.findItem(R.id.actionHideCompleted)).thenReturn(
            hideCompletedItem);

        when(preferences.getShowArchived()).thenReturn(false);
        when(preferences.getShowCompleted()).thenReturn(false);
        when(themeSwitcher.isNightMode()).thenReturn(false);

        menu.onCreate(androidMenu);

        verify(nightModeItem).setChecked(false);
        verify(hideArchivedItem).setChecked(true);
        verify(hideCompletedItem).setChecked(true);
        reset(nightModeItem, hideArchivedItem, hideCompletedItem);

        when(preferences.getShowArchived()).thenReturn(true);
        when(preferences.getShowCompleted()).thenReturn(true);
        when(themeSwitcher.isNightMode()).thenReturn(true);

        menu.onCreate(androidMenu);

        verify(nightModeItem).setChecked(true);
        verify(hideArchivedItem).setChecked(false);
        verify(hideCompletedItem).setChecked(false);
    }

    @Test
    public void testOnSelected_about()
    {
        onItemSelected(R.id.actionAbout);
        verify(behavior).onViewAbout();
    }

    @Test
    public void testOnSelected_add()
    {
        onItemSelected(R.id.actionAdd);
        verify(behavior).onCreateHabit();
    }

    @Test
    public void testOnSelected_faq()
    {
        onItemSelected(R.id.actionFAQ);
        verify(behavior).onViewFAQ();
    }

    @Test
    public void testOnSelected_nightMode()
    {
        onItemSelected(R.id.actionToggleNightMode);
        verify(behavior).onToggleNightMode();
    }

    @Test
    public void testOnSelected_settings()
    {
        onItemSelected(R.id.actionSettings);
        verify(behavior).onViewSettings();
    }

    @Test
    public void testOnSelected_showArchived()
    {
        onItemSelected(R.id.actionHideArchived);
        verify(behavior).onToggleShowArchived();
    }

    @Test
    public void testOnSelected_showCompleted()
    {
        onItemSelected(R.id.actionHideCompleted);
        verify(behavior).onToggleShowCompleted();
    }

    protected void onItemSelected(int actionId)
    {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(actionId);
        menu.onItemSelected(item);
    }
}