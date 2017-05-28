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
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.notifications.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import static org.mockito.Mockito.*;

public class ReminderControllerTest extends BaseAndroidUnitTest
{

    private ReminderController controller;

    private ReminderScheduler reminderScheduler;

    private AndroidNotificationTray notificationTray;

    private AndroidPreferences preferences;

    @Override
    public void setUp()
    {
        super.setUp();

        reminderScheduler = mock(ReminderScheduler.class);
        notificationTray = mock(AndroidNotificationTray.class);
        preferences = mock(AndroidPreferences.class);

        controller = new ReminderController(reminderScheduler,
            notificationTray, preferences);
    }

    @Test
    public void testOnDismiss() throws Exception
    {
        verifyNoMoreInteractions(reminderScheduler);
        verifyNoMoreInteractions(notificationTray);
        verifyNoMoreInteractions(preferences);
    }

    @Test
    public void testOnSnooze() throws Exception
    {
        Habit habit = mock(Habit.class);
        long now = timestamp(2015, 1, 1);
        DateUtils.setFixedLocalTime(now);
        when(preferences.getSnoozeInterval()).thenReturn(15L);

        controller.onSnooze(habit);

        verify(reminderScheduler).schedule(habit, now + 900000);
        verify(notificationTray).cancel(habit);
    }

    @Test
    public void testOnShowReminder() throws Exception
    {
        Habit habit = mock(Habit.class);
        controller.onShowReminder(habit, 123, 456);
        verify(notificationTray).show(habit, 123, 456);
        verify(reminderScheduler).scheduleAll();
    }

    @Test
    public void testOnBootCompleted() throws Exception
    {
        controller.onBootCompleted();
        verify(reminderScheduler).scheduleAll();
    }
}