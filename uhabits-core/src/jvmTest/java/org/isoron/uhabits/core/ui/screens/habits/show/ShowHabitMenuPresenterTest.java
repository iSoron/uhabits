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

package org.isoron.uhabits.core.ui.screens.habits.show;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;

import java.io.*;

import static java.nio.file.Files.*;
import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ShowHabitMenuPresenterTest extends BaseUnitTest
{
    private ShowHabitMenuPresenter.System system;

    private ShowHabitMenuPresenter.Screen screen;

    private Habit habit;

    private ShowHabitMenuPresenter menu;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        system = mock(ShowHabitMenuPresenter.System.class);
        screen = mock(ShowHabitMenuPresenter.Screen.class);

        habit = fixtures.createShortHabit();
        menu = new ShowHabitMenuPresenter(commandRunner, habit, habitList, screen, system, taskRunner);
    }

    @Test
    public void testOnEditHabit()
    {
        menu.onEditHabit();
        verify(screen).showEditHabitScreen(habit);
    }

    @Test
    public void testOnExport() throws Exception
    {
        File outputDir = createTempDirectory("CSV").toFile();
        when(system.getCSVOutputDir()).thenReturn(outputDir);
        menu.onExportCSV();
        assertThat(listFiles(outputDir, null, false).size(), equalTo(1));
        deleteDirectory(outputDir);
    }
}