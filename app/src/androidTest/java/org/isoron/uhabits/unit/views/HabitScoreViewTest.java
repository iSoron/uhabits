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
import android.util.Log;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.isoron.uhabits.views.HabitScoreView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitScoreViewTest extends ViewTest
{
    private Habit habit;
    private HabitScoreView view;

    @Before
    public void setup()
    {
        super.setup();

        HabitFixtures.purgeHabits();
        habit = HabitFixtures.createLongHabit();

        view = new HabitScoreView(targetContext);
        view.setHabit(habit);
        view.setBucketSize(7);
        refreshData(view);
        measureView(dpToPixels(300), dpToPixels(100), view);
    }

    @Test
    public void render() throws Throwable
    {
        Log.d("HabitScoreViewTest", String.format("height=%d", dpToPixels(100)));
        assertRenders(view, "HabitScoreView/render.png");
    }

    @Test
    public void render_withTransparentBackground() throws Throwable
    {
        view.setIsBackgroundTransparent(true);
        assertRenders(view, "HabitScoreView/renderTransparent.png");
    }

    @Test
    public void render_withDifferentSize() throws Throwable
    {
        measureView(dpToPixels(200), dpToPixels(200), view);
        assertRenders(view, "HabitScoreView/renderDifferentSize.png");
    }

    @Test
    public void render_withDataOffset() throws Throwable
    {
        view.onScroll(null, null, -dpToPixels(150), 0);
        view.invalidate();

        assertRenders(view, "HabitScoreView/renderDataOffset.png");
    }

    @Test
    public void render_withMonthlyBucket() throws Throwable
    {
        view.setBucketSize(30);
        view.refreshData();
        view.invalidate();

        assertRenders(view, "HabitScoreView/renderMonthly.png");
    }

    @Test
    public void render_withYearlyBucket() throws Throwable
    {
        view.setBucketSize(365);
        view.refreshData();
        view.invalidate();

        assertRenders(view, "HabitScoreView/renderYearly.png");
    }
}
