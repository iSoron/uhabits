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

import org.hamcrest.number.IsCloseTo;
import org.isoron.uhabits.core.*;
import org.junit.*;
import org.junit.rules.*;

import nl.jqno.equalsverifier.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.closeTo;
import static org.isoron.uhabits.core.utils.DateUtils.*;
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
    public void testActiveState_archived()
    {
        Habit model = modelFactory.buildHabit();
        model.setArchived(true);
        model.getRepetitions().toggle(getToday());

        assertFalse(model.isActive());
    }

    @Test
    public void testActiveState()
    {
        Habit model = modelFactory.buildHabit();

        assertFalse(model.isActive());
        model.getRepetitions().toggle(getToday());
        assertTrue(model.isActive());
    }

    @Test
    public void testTimeoutPercentage()
    {
        Habit model = modelFactory.buildHabit();
        assertThat(model.timeoutPercentage(), is(0f));

        model.getRepetitions().toggle(getToday());
        assertThat(model.timeoutPercentage(), is(1f));
    }

    @Test
    public void testTimeoutPercentage_once_per_period()
    {
        Habit model = modelFactory.buildHabit();

        Timestamp day = getToday().minus(2);
        model.getRepetitions().toggle(day);

        model.setFrequency(new Frequency(1, 3));
        assertThat((double) model.timeoutPercentage(), is(closeTo(0.25f, 0.01f)));

        model.setFrequency(new Frequency(1, 5));
        assertThat((double) model.timeoutPercentage(), is(closeTo(0.5f, 0.01f)));
    }

    @Test
    public void testTimeoutPercentage_several_hits()
    {
        Habit model = modelFactory.buildHabit();

        Timestamp day = getToday().minus(2);
        model.getRepetitions().toggle(day);
        model.getRepetitions().toggle(day.minus(2));

        model.setFrequency(new Frequency(1, 3));
        assertThat((double) model.timeoutPercentage(), is(closeTo(0.25f, 0.01f)));
    }

    @Test
    public void testTimeoutPercentage_every_day()
    {
        Habit model = modelFactory.buildHabit();
        model.setFrequency(new Frequency(1, 1));

        Timestamp day = getToday().minus(1);
        model.getRepetitions().toggle(day);

        assertThat((double) model.timeoutPercentage(), is(closeTo(0.5f, 0.01f)));
    }

    @Test
    @Ignore
    public void testTimeoutPercentage_multiple_per_period()
    {
        Habit model = modelFactory.buildHabit();
        model.setFrequency(new Frequency(2, 5));

        Timestamp day = getToday().minus(2);

        model.getRepetitions().toggle(day);
        model.getRepetitions().toggle(day.minus(2));
        // if we have 2 triggered, this validates 5 days, starting at first day
        // But, current code doesn't handle telling us when the last day we can still chain.
        //TODO Need to change CheckmarkList.getValues(from, to) to fix this?
        assertThat((double) model.timeoutPercentage(), is(closeTo(0.25f, 0.01f)));

        model.setFrequency(new Frequency(1, 5));
        assertThat((double) model.timeoutPercentage(), is(closeTo(0.5f, 0.01f)));
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
        String expected = "{id: <null>, data: {name: , description: ," +
                          " frequency: {numerator: 3, denominator: 7}," +
                          " color: 8, archived: false, targetType: 0," +
                          " targetValue: 100.0, type: 0, unit: ," +
                          " reminder: {hour: 22, minute: 30," +
                          " days: {weekdays: [true,true,true,true,true,true,true]}}," +
                          " position: 0, question: }}";

        assertThat(h.toString(), equalTo(expected));
    }
}