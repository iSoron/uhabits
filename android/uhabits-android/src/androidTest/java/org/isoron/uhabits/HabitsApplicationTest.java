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

package org.isoron.uhabits;

import androidx.test.filters.*;
import androidx.test.runner.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.isoron.androidbase.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitsApplicationTest extends BaseAndroidTest
{
    @Test
    public void test_getLogcat() throws IOException
    {
        String msg = "LOGCAT TEST";
        new RuntimeException(msg).printStackTrace();

        String log = new AndroidBugReporter(targetContext).getLogcat();
        assertThat(log, containsString(msg));
    }
}
