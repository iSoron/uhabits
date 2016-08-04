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

package org.isoron.uhabits;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.memory.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;

public class BaseUnitTest
{
    protected HabitList habitList;

    protected HabitFixtures fixtures;

    protected MemoryModelFactory modelFactory;

    @Before
    public void setUp()
    {
        // 8:00am, January 25th, 2015 (UTC)
        long fixed_local_time = 1422172800000L;
        DateUtils.setFixedLocalTime(fixed_local_time);

        modelFactory = new MemoryModelFactory();
        habitList = modelFactory.buildHabitList();
        fixtures = new HabitFixtures(modelFactory);
    }

    @After
    public void tearDown()
    {
        DateUtils.setFixedLocalTime(null);
    }

    public long timestamp(int year, int month, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }
}
