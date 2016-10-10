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

package org.isoron.uhabits.widgets;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.widgets.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class FrequencyWidgetTest extends BaseViewTest
{
    private static final String PATH = "widgets/FrequencyWidget/";

    private Habit habit;

    private FrameLayout view;

    @Override
    public void setUp()
    {
        super.setUp();
        setTheme(R.style.TransparentWidgetTheme);

        habit = fixtures.createLongHabit();
        FrequencyWidget widget = new FrequencyWidget(targetContext, 0, habit);
        view = convertToView(widget, 400, 400);
    }

    @Test
    public void testIsInstalled()
    {
        assertWidgetProviderIsInstalled(FrequencyWidgetProvider.class);
    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "render.png");
    }
}
