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

package org.isoron.uhabits.core.models;

import org.isoron.uhabits.core.*;
import org.junit.*;
import org.junit.rules.*;

import nl.jqno.equalsverifier.*;

import static org.hamcrest.CoreMatchers.*;
import static org.isoron.uhabits.core.utils.DateUtils.*;
import static org.isoron.uhabits.core.utils.StringUtils.removePointers;
import static org.junit.Assert.*;

public class HabitTest extends BaseUnitTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Test
    public void testConstructor_default()
    {
        Habit habit = modelFactory.buildHabit();
        assertFalse(habit.isArchived());

        assertThat(habit.hasReminder(), is(false));
        assertNotNull(habit.getStreaks());
        assertNotNull(habit.getScores());
        assertNotNull(habit.getRepetitions());
        assertNotNull(habit.getCheckmarks());
    }

    @Test
    public void test_copyAttributes()
    {
        Habit model = modelFactory.buildHabit();
        model.setArchived(true);
        model.setColor(0);
        model.setFrequency(new Frequency(10, 20));
        model.setReminder(new Reminder(8, 30, new WeekdayList(1)));

        Habit habit = modelFactory.buildHabit();
        habit.copyFrom(model);
        assertThat(habit.isArchived(), is(model.isArchived()));
        assertThat(habit.getColor(), is(model.getColor()));
        assertThat(habit.getFrequency(), equalTo(model.getFrequency()));
        assertThat(habit.getReminder(), equalTo(model.getReminder()));
    }


    @Test
    public void test_hasReminder_clearReminder()
    {
        Habit h = modelFactory.buildHabit();
        assertThat(h.hasReminder(), is(false));

        h.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        assertThat(h.hasReminder(), is(true));

        h.clearReminder();
        assertThat(h.hasReminder(), is(false));
    }

    @Test
    public void test_isCompleted() throws Exception
    {
        Habit h = modelFactory.buildHabit();
        assertFalse(h.isCompletedToday());
        h.getRepetitions().toggle(getToday());
        assertTrue(h.isCompletedToday());
    }

    @Test
    public void test_isCompleted_numerical() throws Exception
    {
        Habit h = modelFactory.buildHabit();
        h.setType(Habit.NUMBER_HABIT);
        h.setTargetType(Habit.AT_LEAST);
        h.setTargetValue(100.0);
        assertFalse(h.isCompletedToday());

        h.getRepetitions().toggle(getToday(), 200);
        assertTrue(h.isCompletedToday());
        h.getRepetitions().toggle(getToday(), 100);
        assertTrue(h.isCompletedToday());
        h.getRepetitions().toggle(getToday(), 50);
        assertFalse(h.isCompletedToday());

        h.setTargetType(Habit.AT_MOST);
        h.getRepetitions().toggle(getToday(), 200);
        assertFalse(h.isCompletedToday());
        h.getRepetitions().toggle(getToday(), 100);
        assertTrue(h.isCompletedToday());
        h.getRepetitions().toggle(getToday(), 50);
        assertTrue(h.isCompletedToday());
    }

    @Test
    public void testURI() throws Exception
    {
        assertTrue(habitList.isEmpty());
        Habit h = modelFactory.buildHabit();
        habitList.add(h);
        assertThat(h.getId(), equalTo(0L));
        assertThat(h.getUriString(),
            equalTo("content://org.isoron.uhabits/habit/0"));
    }

    @Test
    public void testEquals() throws Exception
    {
        EqualsVerifier
            .forClass(Habit.HabitData.class)
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();

        EqualsVerifier.forClass(Repetition.class).verify();
        EqualsVerifier.forClass(Score.class).verify();
        EqualsVerifier.forClass(Streak.class).verify();
        EqualsVerifier.forClass(Reminder.class).verify();
        EqualsVerifier.forClass(WeekdayList.class).verify();
    }

    @Test
    public void testToString() throws Exception
    {
        Habit h = modelFactory.buildHabit();
        h.setReminder(new Reminder(22, 30, WeekdayList.EVERY_DAY));

        String s = removePointers(h.toString());

        String expected =
            "org.isoron.uhabits.core.models.Habit@00000000[" +
                "id=<null>," +
                "data=org.isoron.uhabits.core.models.Habit$HabitData@00000000[" +
                    "name=,description=," +
                    "frequency=org.isoron.uhabits.core.models.Frequency@00000000[numerator=3,denominator=7]," +
                    "color=8,archived=false,targetType=0,targetValue=100.0,type=0,unit=," +
                    "reminder=org.isoron.uhabits.core.models.Reminder@00000000[" +
                        "hour=22,minute=30," +
                        "days=org.isoron.uhabits.core.models.WeekdayList@00000000[" +
                            "weekdays={true,true,true,true,true,true,true}]]," +
                    "position=0]]";

        assertThat(s, equalTo(expected));
    }
}