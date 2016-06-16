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

import org.isoron.uhabits.BaseUnitTest;
import org.isoron.uhabits.utils.DateUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class HabitTest extends BaseUnitTest
{

    @Test
    public void testConstructor_default()
    {
        Habit habit = new Habit();
        assertThat(habit.getArchived(), is(0));
        assertThat(habit.getHighlight(), is(0));

        assertThat(habit.hasReminder(), is(false));
        assertThat(habit.getStreaks(), is(not(nullValue())));
        assertThat(habit.getScores(), is(not(nullValue())));
        assertThat(habit.getRepetitions(), is(not(nullValue())));
        assertThat(habit.getCheckmarks(), is(not(nullValue())));
    }

    @Test
    public void test_copyAttributes()
    {
        Habit model = new Habit();
        model.setArchived(1);
        model.setHighlight(1);
        model.setColor(0);
        model.setFreqNum(10);
        model.setFreqDen(20);
        model.setReminder(new Reminder(8, 30, 1));

        Habit habit = new Habit();
        habit.copyFrom(model);
        assertThat(habit.getArchived(), is(model.getArchived()));
        assertThat(habit.getHighlight(), is(model.getHighlight()));
        assertThat(habit.getColor(), is(model.getColor()));
        assertThat(habit.getFreqNum(), is(model.getFreqNum()));
        assertThat(habit.getFreqDen(), is(model.getFreqDen()));
        assertThat(habit.getReminder(), equalTo(model.getReminder()));
    }

//    @Test
//    public  void test_rebuildOrder()
//    {
//        List<Long> ids = new LinkedList<>();
//        int originalPositions[] = { 0, 1, 1, 4, 6, 8, 10, 10, 13};
//
//        for (int p : originalPositions)
//        {
//            Habit h = new Habit();
//            habitList.insert(h);
//            ids.add(h.getId());
//        }
//
//        ((SQLiteHabitList) habitList).rebuildOrder();
//
//        for (int i = 0; i < originalPositions.length; i++)
//        {
//            Habit h = habitList.get(ids.get(i));
//            if(h == null) fail();
//            assertThat(habitList.indexOf(h), is(i));
//        }
//    }


    @Test
    public void test_hasReminder_clearReminder()
    {
        Habit h = new Habit();
        assertThat(h.hasReminder(), is(false));

        h.setReminder(new Reminder(8, 30, DateUtils.ALL_WEEK_DAYS));
        assertThat(h.hasReminder(), is(true));

        h.clearReminder();
        assertThat(h.hasReminder(), is(false));
    }
}