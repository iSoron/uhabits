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

import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.test.*;

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

import javax.inject.*;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class BaseAndroidTest
{
    // 8:00am, January 25th, 2015 (UTC)
    public static final long FIXED_LOCAL_TIME = 1422172800000L;

    private static boolean isLooperPrepared;

    protected Context testContext;

    protected Context targetContext;

    @Inject
    protected Preferences prefs;

    @Inject
    protected HabitList habitList;

    @Inject
    protected CommandRunner commandRunner;

    @Inject
    protected TaskRunner taskRunner;

    @Inject
    protected HabitLogger logger;

    protected AndroidTestComponent androidTestComponent;

    protected HabitFixtures fixtures;

    protected CountDownLatch latch;

    @Before
    public void setUp()
    {
        if (!isLooperPrepared)
        {
            Looper.prepare();
            isLooperPrepared = true;
        }

        targetContext = InstrumentationRegistry.getTargetContext();
        testContext = InstrumentationRegistry.getContext();

        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);
        setTheme(R.style.AppBaseTheme);

        androidTestComponent = DaggerAndroidTestComponent.builder().build();
        HabitsApplication.setComponent(androidTestComponent);
        androidTestComponent.inject(this);

        fixtures = new HabitFixtures(habitList);

        latch = new CountDownLatch(1);
    }

    protected void assertWidgetProviderIsInstalled(Class componentClass)
    {
        ComponentName provider =
            new ComponentName(targetContext, componentClass);
        AppWidgetManager manager = AppWidgetManager.getInstance(targetContext);

        List<ComponentName> installedProviders = new LinkedList<>();
        for (AppWidgetProviderInfo info : manager.getInstalledProviders())
            installedProviders.add(info.provider);

        assertThat(installedProviders, hasItems(provider));
    }

    protected void awaitLatch() throws InterruptedException
    {
        assertTrue(latch.await(60, TimeUnit.SECONDS));
    }

    protected void setTheme(@StyleRes int themeId)
    {
        InterfaceUtils.setFixedTheme(themeId);
        targetContext.setTheme(themeId);
    }

    protected void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            fail();
        }
    }

    protected void waitForAsyncTasks()
    {
        try
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            {
                Thread.sleep(1000);
                return;
            }

            taskRunner.waitForTasks(10000);
        }
        catch (Exception e)
        {
            fail();
        }
    }
}
