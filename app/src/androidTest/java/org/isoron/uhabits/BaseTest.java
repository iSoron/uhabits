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

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.tasks.BaseTask;
import org.junit.Before;

import java.util.concurrent.TimeoutException;

public class BaseTest
{
    protected Context testContext;
    protected Context targetContext;
    private static boolean isLooperPrepared;

    public static final long FIXED_LOCAL_TIME = 1422172800000L; // 8:00am, January 25th, 2015 (UTC)

    @Before
    public void setup()
    {
        if(!isLooperPrepared)
        {
            Looper.prepare();
            isLooperPrepared = true;
        }

        targetContext = InstrumentationRegistry.getTargetContext();
        testContext = InstrumentationRegistry.getContext();

        UIHelper.setFixedTheme(R.style.AppBaseTheme);
        DateHelper.setFixedLocalTime(FIXED_LOCAL_TIME);
    }

    protected void waitForAsyncTasks() throws InterruptedException, TimeoutException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            Thread.sleep(1000);
            return;
        }

        BaseTask.waitForTasks(10000);
    }
}
