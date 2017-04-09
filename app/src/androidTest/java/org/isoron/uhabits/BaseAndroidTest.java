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

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

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

    protected Preferences prefs;

    protected HabitList habitList;

    protected TaskRunner taskRunner;

    protected HabitLogger logger;

    protected HabitFixtures fixtures;

    protected CountDownLatch latch;

    protected AndroidTestComponent component;

    protected ModelFactory modelFactory;

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

        component = DaggerAndroidTestComponent
            .builder()
            .appModule(new AppModule(targetContext.getApplicationContext()))
            .build();

        HabitsApplication.setComponent(component);
        prefs = component.getPreferences();
        habitList = component.getHabitList();
        taskRunner = component.getTaskRunner();
        logger = component.getHabitsLogger();

        modelFactory = component.getModelFactory();
        fixtures = new HabitFixtures(modelFactory, habitList);

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
        targetContext.setTheme(themeId);
        StyledResources.setFixedTheme(themeId);
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
}
