/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.widgets;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;
import org.junit.*;

import java.util.*;

import static org.isoron.uhabits.core.models.Entry.*;
import static org.mockito.Mockito.*;

public class WidgetBehaviorTest extends BaseUnitTest
{
    private NotificationTray notificationTray;

    private CommandRunner commandRunner;

    private Preferences preferences;

    private WidgetBehavior behavior;

    private Habit habit;

    private Timestamp timestamp = new Timestamp(0L);

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();
        commandRunner = mock(CommandRunner.class);
        notificationTray = mock(NotificationTray.class);
        preferences = mock(Preferences.class);
        behavior = new WidgetBehavior(habitList, commandRunner, notificationTray, preferences);
    }

    @Test
    public void testOnAddRepetition()
    {
        behavior.onAddRepetition(habit, timestamp);
        verify(commandRunner).execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, YES_MANUAL),
                habit.id);
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }

    @Test
    public void testOnRemoveRepetition()
    {
        behavior.onRemoveRepetition(habit, timestamp);
        verify(commandRunner).execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, NO),
                habit.id);
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }

    @Test
    public void testOnToggleRepetition()
    {
        for (boolean skipEnabled : Arrays.asList(true, false))
            for (int currentValue : Arrays.asList(NO, YES_MANUAL, YES_AUTO, SKIP))
            {
                when(preferences.isSkipEnabled()).thenReturn(skipEnabled);

                int nextValue;
                if(skipEnabled) nextValue = Entry.Companion.nextToggleValueWithSkip(currentValue);
                else nextValue = Entry.Companion.nextToggleValueWithoutSkip(currentValue);

                habit.getOriginalEntries().setValue(timestamp, currentValue);
                behavior.onToggleRepetition(habit, timestamp);
                verify(preferences).isSkipEnabled();
                verify(commandRunner).execute(
                        new CreateRepetitionCommand(habitList, habit, timestamp, nextValue),
                        habit.id);
                verify(notificationTray).cancel(habit);
                reset(preferences, commandRunner, notificationTray);
            }
    }

    @Test
    public void testOnIncrement()
    {
        habit = fixtures.createNumericalHabit();
        habit.getOriginalEntries().setValue(timestamp, 500);

        behavior.onIncrement(habit, timestamp, 100);
        verify(commandRunner).execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, 600),
                habit.id);
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }

    @Test
    public void testOnDecrement()
    {
        habit = fixtures.createNumericalHabit();
        habit.getOriginalEntries().setValue(timestamp, 500);

        behavior.onDecrement(habit, timestamp, 100);
        verify(commandRunner).execute(
                new CreateRepetitionCommand(habitList, habit, timestamp, 400),
                habit.id);
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }
}
