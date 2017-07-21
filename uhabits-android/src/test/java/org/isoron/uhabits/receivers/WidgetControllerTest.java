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

package org.isoron.uhabits.receivers;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.ui.widgets.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Checkmark.*;
import static org.mockito.Mockito.*;

public class WidgetControllerTest extends BaseAndroidJVMTest
{
    private WidgetBehavior controller;

    private CommandRunner commandRunner;

    private Habit habit;

    private long today;

    private NotificationTray notificationTray;

    @Override
    public void setUp()
    {
        super.setUp();

        today = DateUtils.getStartOfToday();
        habit = fixtures.createEmptyHabit();
        commandRunner = mock(CommandRunner.class);
        notificationTray = mock(NotificationTray.class);
        controller =
            new WidgetBehavior(habitList, commandRunner, notificationTray);
    }

    @Test
    public void testOnAddRepetition_whenChecked() throws Exception
    {
        habit.getRepetitions().toggle(today);
        int todayValue = habit.getCheckmarks().getTodayValue();
        assertThat(todayValue, equalTo(CHECKED_EXPLICITLY));
        controller.onAddRepetition(habit, today);
        verifyZeroInteractions(commandRunner);
    }

    @Test
    public void testOnAddRepetition_whenUnchecked() throws Exception
    {
        int todayValue = habit.getCheckmarks().getTodayValue();
        assertThat(todayValue, equalTo(UNCHECKED));
        controller.onAddRepetition(habit, today);
        verify(commandRunner).execute(any(), isNull());
        verify(notificationTray).cancel(habit);
    }

    @Test
    public void testOnRemoveRepetition_whenChecked() throws Exception
    {
        habit.getRepetitions().toggle(today);
        int todayValue = habit.getCheckmarks().getTodayValue();
        assertThat(todayValue, equalTo(CHECKED_EXPLICITLY));
        controller.onRemoveRepetition(habit, today);
        verify(commandRunner).execute(any(), isNull());
    }

    @Test
    public void testOnRemoveRepetition_whenUnchecked() throws Exception
    {
        int todayValue = habit.getCheckmarks().getTodayValue();
        assertThat(todayValue, equalTo(UNCHECKED));
        controller.onRemoveRepetition(habit, today);
        verifyZeroInteractions(commandRunner);
    }

    @Test
    public void testOnToggleRepetition() throws Exception
    {
        controller.onToggleRepetition(habit, today);
        verify(commandRunner).execute(any(), isNull());
    }
}