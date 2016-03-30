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
import org.isoron.uhabits.views.HabitHistoryView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitHistoryViewTest extends ViewTest
{
    private Habit habit;
    private HabitHistoryView view;

    @Before
    public void setup()
    {
        super.setup();

        HabitFixtures.purgeHabits();
        habit = HabitFixtures.createLongHabit();

        view = new HabitHistoryView(targetContext);
        view.setHabit(habit);
        measureView(dpToPixels(300), dpToPixels(100), view);
    }

    @Test
    public void render() throws Throwable
    {
        assertRenders(view, "HabitHistoryView/render.png");
    }

    @Test
    public void render_withDifferentSize() throws Throwable
    {
        measureView(dpToPixels(200), dpToPixels(200), view);
        assertRenders(view, "HabitHistoryView/renderDifferentSize.png");
    }

    @Test
    public void render_withDataOffset() throws Throwable
    {
        view.onScroll(null, null, -300, 0);
        view.invalidate();

        assertRenders(view, "HabitHistoryView/renderDataOffset.png");
    }

    @Test
    public void tapDate_withEditableView() throws Throwable
    {
        view.setIsEditable(true);
        tap(view, 280, 30);
        waitForAsyncTasks();

        long today = DateHelper.getStartOfToday();
        assertFalse(habit.repetitions.contains(today));
    }

    @Test
    public void tapDate_withReadOnlyView() throws Throwable
    {
        view.setIsEditable(false);
        tap(view, 280, 30);
        waitForAsyncTasks();

        long today = DateHelper.getStartOfToday();
        assertTrue(habit.repetitions.contains(today));
    }

}
