/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.core.commands;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ChangeHabitColorCommandTest extends BaseUnitTest
{
    private ChangeHabitColorCommand command;

    private LinkedList<Habit> selected;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        selected = new LinkedList<>();

        for (int i = 0; i < 3; i++)
        {
            Habit habit = fixtures.createShortHabit();
            habit.setColor(new PaletteColor(i + 1));
            selected.add(habit);
            habitList.add(habit);
        }

        command = new ChangeHabitColorCommand(habitList, selected, new PaletteColor(0));
    }

    @Test
    public void testExecute()
    {
        checkOriginalColors();
        command.run();
        checkNewColors();
    }

    private void checkNewColors()
    {
        for (Habit h : selected)
            assertThat(h.getColor(), equalTo(new PaletteColor(0)));
    }

    private void checkOriginalColors()
    {
        int k = 0;
        for (Habit h : selected)
            assertThat(h.getColor(), equalTo(new PaletteColor(++k)));
    }
}
