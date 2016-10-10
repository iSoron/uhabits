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

package org.isoron.uhabits.widgets.views;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CheckmarkWidgetViewTest extends BaseViewTest
{
    private static final String PATH = "widgets/CheckmarkWidgetView/";

    private CheckmarkWidgetView view;

    private Habit habit;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        setTheme(R.style.TransparentWidgetTheme);

        habit = fixtures.createShortHabit();
        view = new CheckmarkWidgetView(targetContext);
        int color = ColorUtils.getAndroidTestColor(habit.getColor());
        int score = habit.getScores().getTodayValue();
        float percentage = (float) score / Score.MAX_VALUE;

        view.setActiveColor(color);
        view.setCheckmarkValue(habit.getCheckmarks().getTodayValue());
        view.setPercentage(percentage);
        view.setName(habit.getName());
        view.refresh();
        measureView(view, dpToPixels(100), dpToPixels(200));
    }

    @Test
    public void testRender_checked() throws IOException
    {
        assertRenders(view, PATH + "checked.png");
    }

    @Test
    public void testRender_implicitlyChecked() throws IOException
    {
        view.setCheckmarkValue(Checkmark.CHECKED_IMPLICITLY);
        view.refresh();
        assertRenders(view, PATH + "implicitly_checked.png");
    }

    @Test
    public void testRender_largeSize() throws IOException
    {
        measureView(view, dpToPixels(300), dpToPixels(300));
        assertRenders(view, PATH + "large_size.png");
    }

    @Test
    public void testRender_unchecked() throws IOException
    {
        view.setCheckmarkValue(Checkmark.UNCHECKED);
        view.refresh();
        assertRenders(view, PATH + "unchecked.png");
    }
}
