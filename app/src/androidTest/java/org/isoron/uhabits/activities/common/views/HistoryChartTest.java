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

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HistoryChartTest extends BaseViewTest
{
    private static final String BASE_PATH = "common/HistoryChart/";

    private HistoryChart chart;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);
        Habit habit = fixtures.createLongHabit();

        chart = new HistoryChart(targetContext);
        chart.setCheckmarks(habit.getCheckmarks().getAllValues());
        chart.setColor(ColorUtils.getAndroidTestColor(habit.getColor()));
        measureView(chart, dpToPixels(400), dpToPixels(200));
    }

//    @Test
//    public void tapDate_atInvalidLocations() throws Throwable
//    {
//        int expectedCheckmarkValues[] = habit.getCheckmarks().getAllValues();
//
//        chart.setIsEditable(true);
//        tap(chart, 118, 13); // header
//        tap(chart, 336, 60); // tomorrow's square
//        tap(chart, 370, 60); // right axis
//        waitForAsyncTasks();
//
//        int actualCheckmarkValues[] = habit.getCheckmarks().getAllValues();
//        assertThat(actualCheckmarkValues, equalTo(expectedCheckmarkValues));
//    }
//
//    @Test
//    public void tapDate_withEditableView() throws Throwable
//    {
//        chart.setIsEditable(true);
//        tap(chart, 340, 40); // today's square
//        waitForAsyncTasks();
//
//        long today = DateUtils.getStartOfToday();
//        assertFalse(habit.getRepetitions().containsTimestamp(today));
//    }
//
//    @Test
//    public void tapDate_withReadOnlyView() throws Throwable
//    {
//        chart.setIsEditable(false);
//        tap(chart, 340, 40); // today's square
//        waitForAsyncTasks();
//
//        long today = DateUtils.getStartOfToday();
//        assertTrue(habit.getRepetitions().containsTimestamp(today));
//    }

    @Test
    public void testRender() throws Throwable
    {
        assertRenders(chart, BASE_PATH + "render.png");
    }

    @Test
    public void testRender_withDataOffset() throws Throwable
    {
        chart.onScroll(null, null, -dpToPixels(150), 0);
        chart.invalidate();

        assertRenders(chart, BASE_PATH + "renderDataOffset.png");
    }

    @Test
    public void testRender_withDifferentSize() throws Throwable
    {
        measureView(chart, dpToPixels(200), dpToPixels(200));
        assertRenders(chart, BASE_PATH + "renderDifferentSize.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        chart.setIsBackgroundTransparent(true);
        assertRenders(chart, BASE_PATH + "renderTransparent.png");
    }
}
