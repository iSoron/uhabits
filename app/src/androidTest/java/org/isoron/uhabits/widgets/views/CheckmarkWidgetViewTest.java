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

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

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
        InterfaceUtils.setFixedTheme(R.style.TransparentWidgetTheme);

        habit = fixtures.createShortHabit();
        view = new CheckmarkWidgetView(targetContext);
        view.setHabit(habit);
        refreshData(view);
        measureView(dpToPixels(100), dpToPixels(200), view);
    }

    @Test
    public void testRender_checked() throws IOException
    {
        assertRenders(view, PATH + "checked.png");
    }

    @Test
    public void testRender_implicitlyChecked() throws IOException
    {
        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        habit.getRepetitions().toggleTimestamp(today);
        habit.getRepetitions().toggleTimestamp(today - day);
        habit.getRepetitions().toggleTimestamp(today - 2 * day);
        view.refreshData();

        assertRenders(view, PATH + "implicitly_checked.png");
    }

    @Test
    public void testRender_largeSize() throws IOException
    {
        measureView(dpToPixels(300), dpToPixels(300), view);
        assertRenders(view, PATH + "large_size.png");
    }

    @Test
    public void testRender_unchecked() throws IOException
    {
        habit.getRepetitions().toggleTimestamp(DateUtils.getStartOfToday());
        view.refreshData();

        assertRenders(view, PATH + "unchecked.png");
    }
}
