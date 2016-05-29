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

import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.utils.Preferences;
import org.junit.Before;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

public class BaseAndroidTest
{
    protected Context testContext;
    protected Context targetContext;
    private static boolean isLooperPrepared;

    public static final long FIXED_LOCAL_TIME = 1422172800000L; // 8:00am, January 25th, 2015 (UTC)

    @Inject
    protected Preferences prefs;
    protected AndroidTestComponent androidTestComponent;

    @Before
    public void setUp()
    {
        if(!isLooperPrepared)
        {
            Looper.prepare();
            isLooperPrepared = true;
        }

        targetContext = InstrumentationRegistry.getTargetContext();
        testContext = InstrumentationRegistry.getContext();

        InterfaceUtils.setFixedTheme(R.style.AppBaseTheme);
        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);

        androidTestComponent = DaggerAndroidTestComponent.builder().build();
        HabitsApplication.setComponent(androidTestComponent);
        androidTestComponent.inject(this);
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
