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

package org.isoron.uhabits.ui.habits.list.views;

import org.isoron.uhabits.BaseUnitTest;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.list.controllers.HabitCardController;
import org.isoron.uhabits.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HabitCardControllerTest extends BaseUnitTest
{

    private Habit habit;

    private HabitCardController controller;

    private HabitCardController.Listener listener;

    private HabitCardView view;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        this.habit = mock(Habit.class);
        this.listener = mock(HabitCardController.Listener.class);
        this.view = mock(HabitCardView.class);

        this.controller = new HabitCardController();
        controller.setListener(listener);
        controller.setView(view);
        view.setController(controller);
    }

    @Test
    public void testOnInvalidToggle()
    {
        controller.onInvalidToggle();
        verify(listener).onInvalidToggle();
    }

    @Test
    public void testOnToggle()
    {
        long timestamp = DateUtils.getStartOfToday();
        controller.onToggle(habit, timestamp);
        verify(view).triggerRipple(0, 0);
        verify(listener).onToggle(habit, timestamp);
    }
}