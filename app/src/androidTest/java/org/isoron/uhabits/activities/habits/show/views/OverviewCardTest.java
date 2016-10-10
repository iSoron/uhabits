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

package org.isoron.uhabits.activities.habits.show.views;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class OverviewCardTest extends BaseViewTest
{
    public static final String PATH = "habits/show/OverviewCard/";

    private OverviewCard view;

    private Habit habit;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createLongHabit();

        view = (OverviewCard) LayoutInflater
            .from(targetContext)
            .inflate(R.layout.show_habit, null)
            .findViewById(R.id.overviewCard);

        view.setHabit(habit);
        view.refreshData();
        measureView(view, 800, 300);
    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "render.png");
    }
}
