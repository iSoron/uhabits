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

package org.isoron.uhabits.ui.habits.show.views;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.apache.commons.lang3.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.common.views.*;
import org.isoron.uhabits.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitHistoryChartTest extends BaseViewTest
{
    private Habit habit;

    private HistoryChart chart;

    @Before
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);
        habit = fixtures.createLongHabit();

        chart = new HistoryChart(targetContext);
        throw new NotImplementedException("");
//        chart.setHabit(habit);
//        measureView(dpToPixels(400), dpToPixels(200), chart);
//        refreshData(chart);
    }

    @Test
    public void tapDate_atInvalidLocations() throws Throwable
    {
        int expectedCheckmarkValues[] = habit.getCheckmarks().getAllValues();

        chart.setIsEditable(true);
        tap(chart, 118, 13); // header
        tap(chart, 336, 60); // tomorrow's square
        tap(chart, 370, 60); // right axis
        waitForAsyncTasks();

        int actualCheckmarkValues[] = habit.getCheckmarks().getAllValues();
        assertThat(actualCheckmarkValues, equalTo(expectedCheckmarkValues));
    }

    @Test
    public void tapDate_withEditableView() throws Throwable
    {
        chart.setIsEditable(true);
        tap(chart, 340, 40); // today's square
        waitForAsyncTasks();

        long today = DateUtils.getStartOfToday();
        assertFalse(habit.getRepetitions().containsTimestamp(today));
    }

    @Test
    public void tapDate_withReadOnlyView() throws Throwable
    {
        chart.setIsEditable(false);
        tap(chart, 340, 40); // today's square
        waitForAsyncTasks();

        long today = DateUtils.getStartOfToday();
        assertTrue(habit.getRepetitions().containsTimestamp(today));
    }

    @Test
    public void testRender() throws Throwable
    {
        assertRenders(chart, "HabitHistoryView/render.png");
    }

    @Test
    public void testRender_withDataOffset() throws Throwable
    {
        chart.onScroll(null, null, -dpToPixels(150), 0);
        chart.invalidate();

        assertRenders(chart, "HabitHistoryView/renderDataOffset.png");
    }

    @Test
    public void testRender_withDifferentSize() throws Throwable
    {
        measureView(dpToPixels(200), dpToPixels(200), chart);
        assertRenders(chart, "HabitHistoryView/renderDifferentSize.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        chart.setIsBackgroundTransparent(true);
        assertRenders(chart, "HabitHistoryView/renderTransparent.png");
    }
}
