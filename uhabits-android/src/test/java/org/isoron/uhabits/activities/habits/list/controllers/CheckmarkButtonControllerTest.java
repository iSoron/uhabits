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

package org.isoron.uhabits.activities.habits.list.controllers;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.junit.*;

import static org.mockito.Mockito.*;

public class CheckmarkButtonControllerTest extends BaseUnitTest
{
    private CheckmarkButtonController controller;

    private CheckmarkButtonView view;

    private CheckmarkButtonController.Listener listener;

    private Habit habit;

    private int timestamp;

    private Preferences prefs;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        timestamp = 0;
        habit = mock(Habit.class);
        prefs = mock(Preferences.class);

        this.view = mock(CheckmarkButtonView.class);
        this.listener = mock(CheckmarkButtonController.Listener.class);
        this.controller =
            new CheckmarkButtonController(prefs, habit, timestamp);
        controller.setView(view);
        controller.setListener(listener);
    }

    @Test
    public void testOnClick_withShortToggle() throws Exception
    {
        doReturn(true).when(prefs).isShortToggleEnabled();
        controller.onClick();
        verifyToggle();
    }

    @Test
    public void testOnClick_withoutShortToggle() throws Exception
    {
        doReturn(false).when(prefs).isShortToggleEnabled();
        controller.onClick();
        verifyInvalidToggle();
    }

    @Test
    public void testOnLongClick() throws Exception
    {
        controller.onLongClick();
        verifyToggle();
    }

    protected void verifyInvalidToggle()
    {
        verifyZeroInteractions(view);
        verify(listener).onInvalidToggle();
    }

    protected void verifyToggle()
    {
        verify(view).toggle();
        verify(listener).onToggle(habit, timestamp);
    }
}