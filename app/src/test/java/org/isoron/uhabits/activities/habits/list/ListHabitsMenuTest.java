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

import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.junit.*;
import org.mockito.*;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class ListHabitsMenuTest extends BaseUnitTest
{
    private BaseActivity activity;

    private ListHabitsScreen screen;

    private HabitCardListAdapter adapter;

    private Preferences preferences;

    private ThemeSwitcher themeSwitcher;

    private ListHabitsMenu menu;

    private ArgumentCaptor<HabitMatcher> matcherCaptor;

    @Override
    public void setUp()
    {
        super.setUp();

        activity = mock(BaseActivity.class);
        screen = mock(ListHabitsScreen.class);
        adapter = mock(HabitCardListAdapter.class);
        preferences = mock(Preferences.class);
        themeSwitcher = mock(ThemeSwitcher.class);

        when(preferences.getShowArchived()).thenReturn(false);
        when(preferences.getShowCompleted()).thenReturn(false);
        when(themeSwitcher.isNightMode()).thenReturn(false);

        menu = new ListHabitsMenu(activity, screen, adapter, preferences,
            themeSwitcher);

        matcherCaptor = ArgumentCaptor.forClass(HabitMatcher.class);

        reset(adapter);
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

        menu.onCreate(androidMenu);
        verify(nightModeItem).setChecked(false);
        verify(hideArchivedItem).setChecked(true);
        verify(hideCompletedItem).setChecked(true);
        reset(nightModeItem, hideArchivedItem, hideCompletedItem);

        when(themeSwitcher.isNightMode()).thenReturn(true);
        menu.onCreate(androidMenu);
        verify(nightModeItem).setChecked(true);
    }

    @Test
    public void testOnSelected_about()
    {
        onItemSelected(R.id.actionAbout);
        verify(screen).showAboutScreen();
    }

    @Test
    public void testOnSelected_add()
    {
        onItemSelected(R.id.actionAdd);
        verify(screen).showCreateHabitScreen();
    }

    @Test
    public void testOnSelected_faq()
    {
        onItemSelected(R.id.actionFAQ);
        verify(screen).showFAQScreen();
    }

    @Test
    public void testOnSelected_nightMode()
    {
        onItemSelected(R.id.actionToggleNightMode);
        verify(screen).toggleNightMode();
    }

    @Test
    public void testOnSelected_settings()
    {
        onItemSelected(R.id.actionSettings);
        verify(screen).showSettingsScreen();
    }

    @Test
    public void testOnSelected_showArchived()
    {
        onItemSelected(R.id.actionHideArchived);
        verify(preferences).setShowArchived(true);
        verify(adapter).setFilter(matcherCaptor.capture());
        verify(adapter).refresh();
        assertTrue(matcherCaptor.getValue().isArchivedAllowed());
        reset(adapter);

        onItemSelected(R.id.actionHideArchived);
        verify(preferences).setShowArchived(false);
        verify(adapter).setFilter(matcherCaptor.capture());
        verify(adapter).refresh();
        assertFalse(matcherCaptor.getValue().isArchivedAllowed());
    }

    @Test
    public void testOnSelected_showCompleted()
    {
        onItemSelected(R.id.actionHideCompleted);
        verify(preferences).setShowCompleted(true);
        verify(adapter).setFilter(matcherCaptor.capture());
        verify(adapter).refresh();
        assertTrue(matcherCaptor.getValue().isCompletedAllowed());
        reset(adapter);

        onItemSelected(R.id.actionHideCompleted);
        verify(preferences).setShowCompleted(false);
        verify(adapter).setFilter(matcherCaptor.capture());
        verify(adapter).refresh();
        assertFalse(matcherCaptor.getValue().isCompletedAllowed());
    }

    protected void onItemSelected(int actionId)
    {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(actionId);
        menu.onItemSelected(item);
    }
}