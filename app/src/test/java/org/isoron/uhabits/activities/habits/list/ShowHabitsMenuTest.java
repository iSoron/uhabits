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

import android.view.Menu;
import android.view.MenuItem;

import org.isoron.uhabits.BaseUnitTest;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.BaseActivity;
import org.isoron.uhabits.activities.ThemeSwitcher;
import org.isoron.uhabits.activities.habits.list.model.HabitCardListAdapter;
import org.isoron.uhabits.activities.habits.show.ShowHabitActivity;
import org.isoron.uhabits.activities.habits.show.ShowHabitScreen;
import org.isoron.uhabits.activities.habits.show.ShowHabitsMenu;
import org.isoron.uhabits.models.HabitMatcher;
import org.isoron.uhabits.preferences.Preferences;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShowHabitsMenuTest extends BaseUnitTest
{
    private ShowHabitActivity activity;

    private ShowHabitScreen screen;

    private ShowHabitsMenu menu;

    @Override
    public void setUp()
    {
        super.setUp();

        screen = mock(ShowHabitScreen.class);

        menu = new ShowHabitsMenu(activity, screen);
    }

    @Test
    public void testOnEdit_habit()
    {
        onItemSelected(R.id.action_edit_habit);
        verify(screen).showEditHabitDialog();
    }

    @Test
    public void testOnDownload()
    {
        onItemSelected(R.id.download);
        verify(screen).downloadHabit();
    }

    protected void onItemSelected(int actionId)
    {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(actionId);
        menu.onItemSelected(item);
    }
}