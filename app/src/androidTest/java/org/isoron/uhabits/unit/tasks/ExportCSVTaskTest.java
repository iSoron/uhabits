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

package org.isoron.uhabits.unit.tasks;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.BaseAndroidTest;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ExportCSVTask;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ExportCSVTaskTest extends BaseAndroidTest
{
    @Before
    public void setUp()
    {
        super.setUp();
    }

    @Test
    public void testExportCSV() throws Throwable
    {
        HabitFixtures.createShortHabit();
        List<Habit> habits = Habit.getAll(true);

        ExportCSVTask task = new ExportCSVTask(habits, null);
        task.setListener(new ExportCSVTask.Listener()
        {
            @Override
            public void onExportCSVFinished(String archiveFilename)
            {
                assertThat(archiveFilename, is(not(nullValue())));

                File f = new File(archiveFilename);
                assertTrue(f.exists());
                assertTrue(f.canRead());
            }
        });

        task.execute();
        waitForAsyncTasks();
    }
}
