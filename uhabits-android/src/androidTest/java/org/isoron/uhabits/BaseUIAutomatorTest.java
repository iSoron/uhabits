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

import org.junit.*;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.uiautomator.UiDevice.*;

public class BaseUIAutomatorTest
{
    static final String PKG = "org.isoron.uhabits";

    protected UiDevice device;

    @Before
    public void setUp()
    {
        TestButler.setup(getTargetContext());
        TestButler.verifyAnimationsDisabled(getTargetContext());
        device = getInstance(getInstrumentation());
    }

    @After
    public void tearDown()
    {
        TestButler.teardown(getTargetContext());
    }

    protected void startActivity(Class cls)
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PKG, cls.getCanonicalName()));
        getContext().startActivity(intent);
    }
}
