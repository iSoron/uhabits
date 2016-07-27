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

import org.isoron.uhabits.activities.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitsApplicationTest extends BaseAndroidTest
{
    @Test
    public void test_getLogcat() throws IOException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return;

        String msg = "LOGCAT TEST";
        new RuntimeException(msg).printStackTrace();

        BaseSystem system = new BaseSystem(targetContext);
        String log = system.getLogcat();
        assertThat(log, containsString(msg));
    }
}
