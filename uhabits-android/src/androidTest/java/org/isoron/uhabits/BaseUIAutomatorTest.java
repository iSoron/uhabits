/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.*;
import android.support.test.uiautomator.*;

import com.linkedin.android.testbutler.*;

import org.isoron.androidbase.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.io.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.uiautomator.UiDevice.*;

public class BaseUIAutomatorTest
{
    private static final String PKG = "org.isoron.uhabits";

    public static UiDevice device;

    private HabitsComponent component;

    @Before
    public void setUp() throws IOException
    {
        TestButler.setup(getTargetContext());
        TestButler.verifyAnimationsDisabled(getTargetContext());
        device = getInstance(getInstrumentation());

        component = DaggerHabitsComponent
            .builder()
            .appModule(new AppModule(getTargetContext()))
            .build();

        AndroidPreferences prefs = component.getPreferences();
        prefs.reset();
        prefs.setFirstRun(false);

        HabitsApplication.setComponent(component);

        FileUtils.copy(getContext().getAssets().open("test.db"),
            DatabaseUtils.getDatabaseFile(getTargetContext()));
    }

    @After
    public void tearDown() throws Exception
    {
        device.pressHome();
        device.waitForIdle();
        TestButler.teardown(getTargetContext());
    }

    public static void startActivity(Class cls)
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PKG, cls.getCanonicalName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getContext().startActivity(intent);
    }
}
