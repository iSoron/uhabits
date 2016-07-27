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

import android.os.*;
import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitLoggerTest extends BaseAndroidTest
{
    @Test
    public void testLogReminderScheduled() throws IOException
    {
        if (!isLogcatAvailable()) return;

        long time = 1422277200000L; // 13:00 jan 26, 2015 (UTC)
        Habit habit = fixtures.createEmptyHabit();
        habit.setName("Write journal");

        logger.logReminderScheduled(habit, time);

        String expectedMsg = "Setting alarm (2015-01-26 130000): Wri\n";
        assertLogcatContains(expectedMsg);
    }

    protected void assertLogcatContains(String expectedMsg) throws IOException
    {
        BaseSystem system = new BaseSystem(targetContext);
        String logcat = system.getLogcat();
        assertThat(logcat, containsString(expectedMsg));
    }

    protected boolean isLogcatAvailable()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
}
