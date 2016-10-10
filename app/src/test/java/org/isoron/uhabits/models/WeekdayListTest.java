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

package org.isoron.uhabits.models;

import org.isoron.uhabits.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;

public class WeekdayListTest extends BaseUnitTest
{
    @Test
    public void test()
    {
        int daysInt = 124;
        boolean[] daysArray = new boolean[]{
            false, false, true, true, true, true, true
        };

        WeekdayList list = new WeekdayList(daysArray);
        assertThat(list.toArray(), equalTo(daysArray));
        assertThat(list.toInteger(), equalTo(daysInt));

        list = new WeekdayList(daysInt);
        assertThat(list.toArray(), equalTo(daysArray));
        assertThat(list.toInteger(), equalTo(daysInt));
    }

    @Test
    public void testEmpty()
    {
        WeekdayList list = new WeekdayList(0);
        assertThat(list.toArray(), equalTo(WeekdayList.EVERY_DAY.toArray()));
    }
}
