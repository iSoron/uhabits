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

import android.view.MenuItem;

import org.isoron.uhabits.BaseUnitTest;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.show.ShowHabitActivity;
import org.isoron.uhabits.activities.habits.show.ShowHabitScreen;
import org.isoron.uhabits.activities.habits.show.ShowHabitsMenu;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ExportCSVTask;
import org.isoron.uhabits.tasks.ExportCSVTaskFactory;
import org.isoron.uhabits.tasks.TaskRunner;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShowHabitsMenuTest extends BaseUnitTest {
    private ShowHabitActivity activity;

    private ShowHabitScreen screen;

    private ShowHabitsMenu menu;

    private Habit habit;

    private ExportCSVTaskFactory exportCSVFactory;

    private TaskRunner taskRunner;

    private ExportCSVTask task;

    @Override
    public void setUp() {
        super.setUp();
        activity = mock(ShowHabitActivity.class);
        screen = mock(ShowHabitScreen.class);
        habit = mock(Habit.class);
        exportCSVFactory = mock(ExportCSVTaskFactory.class);
        taskRunner = mock(TaskRunner.class);
        menu = new ShowHabitsMenu(activity, screen, habit, exportCSVFactory, taskRunner);
    }

    @Test
    public void testOnEditHabit() {
        onItemSelected(R.id.action_edit_habit);
        verify(screen).showEditHabitDialog();
    }

    @Test
    public void testOnDownloadHabit() {
        onItemSelected(R.id.download);
        verify(taskRunner).execute(any());
    }

    protected void onItemSelected(int actionId) {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(actionId);
        menu.onItemSelected(item);
    }
}