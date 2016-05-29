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

import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.isoron.uhabits.views.HabitHistoryView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitHistoryViewTest extends ViewTest
{
    private Habit habit;
    private HabitHistoryView view;

    @Before
    public void setUp()
    {
        super.setUp();

        HabitFixtures.purgeHabits();
        habit = HabitFixtures.createLongHabit();

        view = new HabitHistoryView(targetContext);
        view.setHabit(habit);
        measureView(dpToPixels(400), dpToPixels(200), view);
        refreshData(view);
    }

    @Test
    public void testRender() throws Throwable
    {
        assertRenders(view, "HabitHistoryView/render.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        view.setIsBackgroundTransparent(true);
        assertRenders(view, "HabitHistoryView/renderTransparent.png");
    }

    @Test
    public void testRender_withDifferentSize() throws Throwable
    {
        measureView(dpToPixels(200), dpToPixels(200), view);
        assertRenders(view, "HabitHistoryView/renderDifferentSize.png");
    }

    @Test
    public void testRender_withDataOffset() throws Throwable
    {
        view.onScroll(null, null, -dpToPixels(150), 0);
        view.invalidate();

        assertRenders(view, "HabitHistoryView/renderDataOffset.png");
    }

    @Test
    public void tapDate_withEditableView() throws Throwable
    {
        view.setIsEditable(true);
        tap(view, 340, 40); // today's square
        waitForAsyncTasks();

        long today = DateUtils.getStartOfToday();
        assertFalse(habit.repetitions.contains(today));
    }

    @Test
    public void tapDate_atInvalidLocations() throws Throwable
    {
        int expectedCheckmarkValues[] = habit.checkmarks.getAllValues();

        view.setIsEditable(true);
        tap(view, 118, 13); // header
        tap(view, 336, 60); // tomorrow's square
        tap(view, 370, 60); // right axis
        waitForAsyncTasks();

        int actualCheckmarkValues[] = habit.checkmarks.getAllValues();
        assertThat(actualCheckmarkValues, equalTo(expectedCheckmarkValues));
    }

    @Test
    public void tapDate_withReadOnlyView() throws Throwable
    {
        view.setIsEditable(false);
        tap(view, 340, 40); // today's square
        waitForAsyncTasks();

        long today = DateUtils.getStartOfToday();
        assertTrue(habit.repetitions.contains(today));
    }

}
