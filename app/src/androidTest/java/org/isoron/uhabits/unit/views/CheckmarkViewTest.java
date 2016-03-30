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

package org.isoron.uhabits.unit.views;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.isoron.uhabits.views.CheckmarkView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CheckmarkViewTest extends ViewTest
{
    private CheckmarkView view;
    private Habit habit;

    @Before
    public void setup()
    {
        super.setup();

        habit = HabitFixtures.createNonDailyHabit();
        view = new CheckmarkView(targetContext);
        view.setHabit(habit);
        measureView(dpToPixels(100), dpToPixels(200), view);
    }

    @Test
    public void render_checked() throws IOException
    {
        assertRenders(view, "CheckmarkView/checked.png");
    }

    @Test
    public void render_unchecked() throws IOException
    {
        habit.repetitions.toggle(DateHelper.getStartOfToday());
        view.refreshData();

        assertRenders(view, "CheckmarkView/unchecked.png");
    }

    @Test
    public void render_implicitlyChecked() throws IOException
    {
        long today = DateHelper.getStartOfToday();
        long day = DateHelper.millisecondsInOneDay;
        habit.repetitions.toggle(today);
        habit.repetitions.toggle(today - day);
        habit.repetitions.toggle(today - 2 * day);
        view.refreshData();

        assertRenders(view, "CheckmarkView/implicitly_checked.png");
    }

    @Test
    public void render_largeSize() throws IOException
    {
        measureView(dpToPixels(300), dpToPixels(300), view);
        assertRenders(view, "CheckmarkView/large_size.png");
    }
}
