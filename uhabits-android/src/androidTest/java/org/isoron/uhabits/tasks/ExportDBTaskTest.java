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

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ExportDBTaskTest extends BaseAndroidTest
{
    @Override
    @Before
    public void setUp()
    {
        super.setUp();
    }

//    @Test
//    public void testExportCSV() throws Throwable
//    {
//        ExportDBTask task =
//            new ExportDBTask(targetContext, new AndroidDirFinder(targetContext),
//                filename ->
//                {
//                    assertNotNull(filename);
//                    File f = new File(filename);
//                    assertTrue(f.exists());
//                    assertTrue(f.canRead());
//                });
//
//        taskRunner.execute(task);
//    }
}
