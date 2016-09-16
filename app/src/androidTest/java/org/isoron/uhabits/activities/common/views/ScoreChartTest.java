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

package org.isoron.uhabits.activities.common.views;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ScoreChartTest extends BaseViewTest
{
    private static final String BASE_PATH = "common/ScoreChart/";

    private Habit habit;

    private ScoreChart view;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);
        habit = fixtures.createLongHabit();

        view = new ScoreChart(targetContext);
        view.setScores(habit.getScores().toList());
        view.setColor(ColorUtils.getColor(targetContext, habit.getColor()));
        view.setBucketSize(7);
        measureView(view, dpToPixels(300), dpToPixels(200));
    }

    @Test
    public void testRender() throws Throwable
    {
        Log.d("HabitScoreViewTest",
            String.format("height=%d", dpToPixels(100)));
        assertRenders(view, BASE_PATH + "render.png");
    }

    @Test
    public void testRender_withDataOffset() throws Throwable
    {
        view.onScroll(null, null, -dpToPixels(150), 0);
        view.invalidate();

        assertRenders(view, BASE_PATH + "renderDataOffset.png");
    }

    @Test
    public void testRender_withDifferentSize() throws Throwable
    {
        measureView(view, dpToPixels(200), dpToPixels(200));
        assertRenders(view, BASE_PATH + "renderDifferentSize.png");
    }

    @Test
    public void testRender_withMonthlyBucket() throws Throwable
    {
        view.setScores(habit.getScores().groupBy(DateUtils.TruncateField.MONTH));
        view.setBucketSize(30);
        view.invalidate();

        assertRenders(view, BASE_PATH + "renderMonthly.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        view.setIsTransparencyEnabled(true);
        assertRenders(view, BASE_PATH + "renderTransparent.png");
    }

    @Test
    public void testRender_withYearlyBucket() throws Throwable
    {
        view.setScores(habit.getScores().groupBy(DateUtils.TruncateField.YEAR));
        view.setBucketSize(365);
        view.invalidate();

        assertRenders(view, BASE_PATH + "renderYearly.png");
    }
}
