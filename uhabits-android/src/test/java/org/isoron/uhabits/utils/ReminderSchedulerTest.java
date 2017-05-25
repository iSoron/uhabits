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

package org.isoron.uhabits.utils;

import android.app.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.models.*;
import org.junit.*;

import java.util.*;

import static org.mockito.Mockito.*;

@SuppressWarnings("JavaDoc")
public class ReminderSchedulerTest extends BaseUnitTest
{
    private Habit habit;

    private PendingIntent intent;

    private ReminderScheduler reminderScheduler;

    private HabitLogger logger;

    private PendingIntentFactory pendingIntentFactory;

    private IntentScheduler intentScheduler;

    private CommandRunner commandRunner;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        intent = mock(PendingIntent.class);
        logger = mock(HabitLogger.class);
        pendingIntentFactory = mock(PendingIntentFactory.class);
        intentScheduler = mock(IntentScheduler.class);
        commandRunner = mock(CommandRunner.class);

        reminderScheduler =
            new ReminderScheduler(pendingIntentFactory, intentScheduler, logger,
                commandRunner, habitList);
        habit = fixtures.createEmptyHabit();

        DateUtils.setFixedTimeZone(TimeZone.getTimeZone("GMT-4"));
    }

    @Test
    public void testSchedule_atSpecificTime()
    {
        long atTime = timestamp(2015, 1, 30, 11, 30);
        long expectedCheckmarkTime = timestamp(2015, 1, 30, 0, 0);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(atTime, expectedCheckmarkTime, atTime);
    }

    @Test
    public void testSchedule_laterToday()
    {
        long now = timestamp(2015, 1, 26, 6, 30);
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = timestamp(2015, 1, 26, 0, 0);
        long expectedReminderTime = timestamp(2015, 1, 26, 12, 30);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));

        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_list()
    {
        long now = timestamp(2015, 1, 26, 13, 0);
        DateUtils.setFixedLocalTime(now);

        Habit h1 = fixtures.createEmptyHabit();
        h1.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        habitList.add(h1);

        Habit h2 = fixtures.createEmptyHabit();
        h2.setReminder(new Reminder(18, 30, WeekdayList.EVERY_DAY));
        habitList.add(h2);

        Habit h3 = fixtures.createEmptyHabit();
        habitList.add(h3);

        reminderScheduler.scheduleAll();

        verify(intentScheduler).schedule(timestamp(2015, 1, 27, 12, 30), null);
        verify(intentScheduler).schedule(timestamp(2015, 1, 26, 22, 30), null);
        verifyNoMoreInteractions(intentScheduler);
    }

    @Test
    public void testSchedule_tomorrow()
    {
        long now = timestamp(2015, 1, 26, 13, 0);
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = timestamp(2015, 1, 27, 0, 0);
        long expectedReminderTime = timestamp(2015, 1, 27, 12, 30);

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_withoutReminder()
    {
        reminderScheduler.schedule(habit, null);
        verifyZeroInteractions(intentScheduler);
    }

    public long timestamp(int year, int month, int day, int hour, int minute)
    {
        Calendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day, hour, minute);
        return cal.getTimeInMillis();
    }

    private void scheduleAndVerify(Long atTime,
                                   long expectedCheckmarkTime,
                                   long expectedReminderTime)
    {
        when(pendingIntentFactory.showReminder(habit, expectedReminderTime,
            expectedCheckmarkTime)).thenReturn(intent);

        reminderScheduler.schedule(habit, atTime);

        verify(logger).logReminderScheduled(habit, expectedReminderTime);

        verify(pendingIntentFactory).showReminder(habit, expectedReminderTime,
            expectedCheckmarkTime);
        verify(intentScheduler).schedule(expectedReminderTime, intent);
    }
}
