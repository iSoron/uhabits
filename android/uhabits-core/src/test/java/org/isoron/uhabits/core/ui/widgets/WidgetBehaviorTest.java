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
import org.isoron.uhabits.core.utils.*;
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

    private Timestamp today;

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
        today = DateUtils.getTodayWithOffset();
    }

    @Test
    public void testOnAddRepetition()
    {
        behavior.onAddRepetition(habit, today);
        verify(commandRunner).run(
                new CreateRepetitionCommand(habitList, habit, today, YES_MANUAL)
        );
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }

    @Test
    public void testOnRemoveRepetition()
    {
        behavior.onRemoveRepetition(habit, today);
        verify(commandRunner).run(
                new CreateRepetitionCommand(habitList, habit, today, NO)
        );
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

                habit.getOriginalEntries().add(new Entry(today, currentValue));
                behavior.onToggleRepetition(habit, today);
                verify(preferences).isSkipEnabled();
                verify(commandRunner).run(
                        new CreateRepetitionCommand(habitList, habit, today, nextValue)
                );
                verify(notificationTray).cancel(habit);
                reset(preferences, commandRunner, notificationTray);
            }
    }

    @Test
    public void testOnIncrement()
    {
        habit = fixtures.createNumericalHabit();
        habit.getOriginalEntries().add(new Entry(today, 500));
        habit.recompute();

        behavior.onIncrement(habit, today, 100);
        verify(commandRunner).run(
                new CreateRepetitionCommand(habitList, habit, today, 600)
        );
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }

    @Test
    public void testOnDecrement()
    {
        habit = fixtures.createNumericalHabit();
        habit.getOriginalEntries().add(new Entry(today, 500));
        habit.recompute();

        behavior.onDecrement(habit, today, 100);
        verify(commandRunner).run(
                new CreateRepetitionCommand(habitList, habit, today, 400)
        );
        verify(notificationTray).cancel(habit);
        verifyZeroInteractions(preferences);
    }
}
