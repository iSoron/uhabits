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

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.BaseAndroidTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ExportDBTaskTest extends BaseAndroidTest
{
    @Before
    public void setUp()
    {
        super.setUp();
    }

    @Test
    public void testExportCSV() throws Throwable
    {
        ExportDBTask task = new ExportDBTask(null);
        task.setListener(new ExportDBTask.Listener()
        {
            @Override
            public void onExportDBFinished(String filename)
            {
                assertThat(filename, is(not(nullValue())));

                File f = new File(filename);
                assertTrue(f.exists());
                assertTrue(f.canRead());
            }
        });

        task.execute();
        waitForAsyncTasks();
    }
}
