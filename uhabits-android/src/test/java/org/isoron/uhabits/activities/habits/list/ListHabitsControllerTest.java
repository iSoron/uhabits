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

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.*;
import org.junit.*;

import static org.mockito.Mockito.*;

public class ListHabitsControllerTest extends BaseUnitTest
{

    private ListHabitsController controller;

    private ImportDataTaskFactory importTaskFactory;

    private BaseSystem system;

    private CommandRunner commandRunner;

    private HabitCardListAdapter adapter;

    private ListHabitsScreen screen;

    private Preferences prefs;

    private ReminderScheduler reminderScheduler;

    private SingleThreadTaskRunner taskRunner;

    private WidgetUpdater widgetUpdater;

    private ExportCSVTaskFactory exportCSVFactory;

    private ExportDBTaskFactory exportDBFactory;

    @Override
    public void setUp()
    {
        super.setUp();

        habitList = mock(HabitList.class);
        system = mock(BaseSystem.class);
        commandRunner = mock(CommandRunner.class);
        adapter = mock(HabitCardListAdapter.class);
        screen = mock(ListHabitsScreen.class);
        prefs = mock(Preferences.class);
        reminderScheduler = mock(ReminderScheduler.class);
        taskRunner = new SingleThreadTaskRunner();
        widgetUpdater = mock(WidgetUpdater.class);
        importTaskFactory = mock(ImportDataTaskFactory.class);
        exportCSVFactory = mock(ExportCSVTaskFactory.class);
        exportDBFactory = mock(ExportDBTaskFactory.class);

        controller =
            spy(new ListHabitsController(system, commandRunner, habitList,
                adapter, screen, prefs, reminderScheduler, taskRunner,
                widgetUpdater, importTaskFactory, exportCSVFactory, exportDBFactory));
    }

    @Test
    public void testOnHabitClick()
    {
        Habit h = mock(Habit.class);
        controller.onHabitClick(h);
        verify(screen).showHabitScreen(h);
    }

    @Test
    public void testOnHabitReorder()
    {
        Habit from = mock(Habit.class);
        Habit to = mock(Habit.class);
        controller.onHabitReorder(from, to);
        verify(habitList).reorder(from, to);
    }

    @Test
    public void onInvalidToggle()
    {
        controller.onInvalidToggle();
        verify(screen).showMessage(R.string.long_press_to_toggle);
    }

    @Test
    public void onStartup_notFirstLaunch()
    {
        when(prefs.isFirstRun()).thenReturn(false);
        controller.onStartup();
        verify(prefs).incrementLaunchCount();
    }

    @Test
    public void onStartup_firstLaunch()
    {
        long today = DateUtils.getStartOfToday();

        when(prefs.isFirstRun()).thenReturn(true);
        controller.onStartup();
        verify(prefs).setFirstRun(false);
        verify(prefs).updateLastHint(-1, today);
        verify(screen).showIntroScreen();
    }
}