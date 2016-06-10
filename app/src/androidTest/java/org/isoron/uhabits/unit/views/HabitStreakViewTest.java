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

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.show.views.HabitStreakView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitStreakViewTest extends ViewTest
{
    private HabitStreakView view;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        habitFixtures.purgeHabits(habitList);
        Habit habit = habitFixtures.createLongHabit();

        view = new HabitStreakView(targetContext);
        measureView(dpToPixels(300), dpToPixels(100), view);

        view.setHabit(habit);
        refreshData(view);
    }

    @Test
    public void testRender() throws Throwable
    {
        assertRenders(view, "HabitStreakView/render.png");
    }

    @Test
    public void testRender_withSmallSize() throws Throwable
    {
        measureView(dpToPixels(100), dpToPixels(100), view);
        refreshData(view);

        assertRenders(view, "HabitStreakView/renderSmallSize.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        view.setIsBackgroundTransparent(true);
        assertRenders(view, "HabitStreakView/renderTransparent.png");
    }
}
