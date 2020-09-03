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
 *
 *
 */

package org.isoron.uhabits.core.models.sqlite.records;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;

public class HabitRecordTest extends BaseUnitTest
{

    @Test
    public void testCopyRestore1()
    {
        Habit original = modelFactory.buildHabit();
        original.setName("Hello world");
        original.setQuestion("Did you greet the world today?");
        original.setColor(1);
        original.setArchived(true);
        original.setFrequency(Frequency.THREE_TIMES_PER_WEEK);
        original.setReminder(new Reminder(8, 30));
        original.setId(1000L);
        original.setPosition(20);

        HabitRecord record = new HabitRecord();
        record.copyFrom(original);

        Habit duplicate = modelFactory.buildHabit();
        record.copyTo(duplicate);

        assertThat(original.getData(), equalTo(duplicate.getData()));
    }

    @Test
    public void testCopyRestore2()
    {
        Habit original = modelFactory.buildHabit();
        original.setName("Hello world");
        original.setQuestion("Did you greet the world today?");
        original.setColor(5);
        original.setArchived(false);
        original.setFrequency(Frequency.DAILY);
        original.setReminder(null);
        original.setId(1L);
        original.setPosition(15);
        original.setType(Habit.NUMBER_HABIT);
        original.setTargetValue(100);
        original.setTargetType(Habit.AT_LEAST);
        original.setUnit("miles");

        HabitRecord record = new HabitRecord();
        record.copyFrom(original);

        Habit duplicate = modelFactory.buildHabit();
        record.copyTo(duplicate);

        assertThat(original.getData(), equalTo(duplicate.getData()));
    }
}
