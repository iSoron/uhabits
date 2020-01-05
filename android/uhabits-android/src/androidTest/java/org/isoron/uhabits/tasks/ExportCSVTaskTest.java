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

package org.isoron.uhabits.tasks;

import androidx.test.filters.*;
import androidx.test.runner.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ExportCSVTaskTest extends BaseAndroidTest
{
    @Before
    @Override
    public void setUp()
    {
        super.setUp();
    }

//    @Test
//    public void testExportCSV() throws Throwable
//    {
//        fixtures.purgeHabits(habitList);
//        fixtures.createShortHabit();
//
//        List<Habit> selected = new LinkedList<>();
//        for (Habit h : habitList) selected.add(h);
//        File outputDir = new AndroidDirFinder(targetContext).getFilesDir("CSV");
//        assertNotNull(outputDir);
//
//        taskRunner.execute(
//            new ExportCSVTask(habitList, selected, outputDir, archiveFilename -> {
//                assertThat(archiveFilename, is(not(nullValue())));
//                File f = new File(archiveFilename);
//                assertTrue(f.exists());
//                assertTrue(f.canRead());
//            }));
//    }
}
