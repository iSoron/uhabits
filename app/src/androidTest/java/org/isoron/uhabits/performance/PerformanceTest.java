/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.performance;

import android.support.test.filters.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.junit.*;

@MediumTest
public class PerformanceTest extends BaseAndroidTest
{
    private Habit habit;

    @Override
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createLongHabit();
    }

    @Test(timeout = 1000)
    public void testRepeatedGetTodayValue()
    {
        for (int i = 0; i < 100000; i++)
        {
            habit.getScores().getTodayValue();
            habit.getCheckmarks().getTodayValue();
        }
    }

}
