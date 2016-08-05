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

package org.isoron.uhabits.activities.habits.list.views;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitCardViewTest extends BaseViewTest
{
    private HabitCardView view;

    public static final String PATH = "habits/list/HabitCardView/";

    private HabitCardView.Controller controller;

    private Habit habit;

    @Override
    public void setUp()
    {
        super.setUp();
        setTheme(R.style.AppBaseTheme);

        habit = fixtures.createLongHabit();
        CheckmarkList checkmarks = habit.getCheckmarks();

        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        int[] values = checkmarks.getValues(today - 5 * day, today);

        controller = mock(HabitCardView.Controller.class);

        view = new HabitCardView(targetContext);
        view.setHabit(habit);
        view.setCheckmarkValues(values);
        view.setSelected(false);
        view.setScore(habit.getScores().getTodayValue());
        view.setController(controller);
        measureView(view, dpToPixels(400), dpToPixels(50));
    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "render.png");
    }

    @Test
    public void testRender_selected() throws Exception
    {
        view.setSelected(true);
        measureView(view, dpToPixels(400), dpToPixels(50));
        assertRenders(view, PATH + "render_selected.png");
    }

    @Test
    public void testChangeModel() throws Exception
    {
        habit.setName("Wake up early");
        habit.setColor(2);
        habit.getObservable().notifyListeners();
        assertRenders(view, PATH + "render_changed.png");
    }
}
