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

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.records.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;

public class HabitRecordTest extends BaseUnitTest
{
    private Habit habit;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        habit = modelFactory.buildHabit();
        habit.setName("Hello world");
        habit.setDescription("Did you greet the world today?");
        habit.setColor(1);
        habit.setArchived(true);
        habit.setFrequency(Frequency.THREE_TIMES_PER_WEEK);
        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        habit.setId(1000L);
    }

    @Test
    public void testCopyFrom()
    {
        HabitRecord rec = new HabitRecord();
        rec.copyFrom(habit);

        assertThat(rec.name, equalTo(habit.getName()));
        assertThat(rec.description, equalTo(habit.getDescription()));
        assertThat(rec.color, equalTo(habit.getColor()));
        assertThat(rec.archived, equalTo(1));
        assertThat(rec.freqDen, equalTo(7));
        assertThat(rec.freqNum, equalTo(3));

        Reminder reminder = habit.getReminder();
        assertThat(rec.reminderDays, equalTo(reminder.getDays().toInteger()));
        assertThat(rec.reminderHour, equalTo(reminder.getHour()));
        assertThat(rec.reminderMin, equalTo(reminder.getMinute()));

        habit.setReminder(null);
        rec.copyFrom(habit);

        assertThat(rec.reminderMin, equalTo(null));
        assertThat(rec.reminderHour, equalTo(null));
        assertThat(rec.reminderDays, equalTo(0));
    }
}
