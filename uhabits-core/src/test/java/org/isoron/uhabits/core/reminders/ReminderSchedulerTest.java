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

package org.isoron.uhabits.core.reminders;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReminderSchedulerTest extends BaseUnitTest
{
    private Habit habit;

    private ReminderScheduler reminderScheduler;

    @Mock
    private ReminderScheduler.SystemScheduler sys;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        habit = fixtures.createEmptyHabit();

        reminderScheduler =
            new ReminderScheduler(commandRunner, habitList, sys);

        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("GMT-4"));
    }

    @Test
    public void testScheduleAll()
    {
        long now = unixTime(2015, 1, 26, 13, 0);
        DateUtils.setFixedLocalTime(now);

        Habit h1 = fixtures.createEmptyHabit();
        Habit h2 = fixtures.createEmptyHabit();
        Habit h3 = fixtures.createEmptyHabit();
        h1.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        h2.setReminder(new Reminder(18, 30, WeekdayList.EVERY_DAY));
        h3.setReminder(null);
        habitList.add(h1);
        habitList.add(h2);
        habitList.add(h3);

        reminderScheduler.scheduleAll();

        verify(sys).scheduleShowReminder(eq(unixTime(2015, 1, 27, 12, 30)),
            eq(h1), anyLong());
        verify(sys).scheduleShowReminder(eq(unixTime(2015, 1, 26, 22, 30)),
            eq(h2), anyLong());
        Mockito.verifyNoMoreInteractions(sys);
    }

    @Test
    public void testSchedule_atSpecificTime()
    {
        long atTime = unixTime(2015, 1, 30, 11, 30);
        long expectedCheckmarkTime = unixTime(2015, 1, 30, 0, 0);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(atTime, expectedCheckmarkTime, atTime);
    }

    @Test
    public void testSchedule_laterToday()
    {
        long now = unixTime(2015, 1, 26, 6, 30);
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = unixTime(2015, 1, 26, 0, 0);
        long expectedReminderTime = unixTime(2015, 1, 26, 12, 30);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_tomorrow()
    {
        long now = unixTime(2015, 1, 26, 13, 0);
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = unixTime(2015, 1, 27, 0, 0);
        long expectedReminderTime = unixTime(2015, 1, 27, 12, 30);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_withoutReminder()
    {
        reminderScheduler.schedule(habit);
        Mockito.verifyZeroInteractions(sys);
    }

    public long unixTime(int year, int month, int day, int hour, int minute)
    {
        Calendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day, hour, minute);
        return cal.getTimeInMillis();
    }

    private void scheduleAndVerify(Long atTime,
                                   long expectedCheckmarkTime,
                                   long expectedReminderTime)
    {
        if(atTime == null) reminderScheduler.schedule(habit);
        else reminderScheduler.scheduleAtTime(habit, atTime);
        verify(sys).scheduleShowReminder(expectedReminderTime, habit,
            expectedCheckmarkTime);
    }
}
