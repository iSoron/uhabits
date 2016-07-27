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
import org.isoron.uhabits.models.*;
import org.junit.*;

import static org.mockito.Mockito.*;

@SuppressWarnings("JavaDoc")
public class ReminderSchedulerTest extends BaseUnitTest
{
    private Habit habit;

    private PendingIntent intent;

    private ReminderScheduler reminderScheduler;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        intent = mock(PendingIntent.class);
        reminderScheduler =
            new ReminderScheduler(pendingIntentFactory, intentScheduler,
                logger);
        habit = fixtures.createEmptyHabit();
    }

    @Test
    public void testSchedule_atSpecificTime()
    {
        long atTime = 1422617400000L; // 11:30 jan 30, 2015 (UTC)
        long expectedCheckmarkTime = 1422576000000L; // 00:00 jan 27, 2015 (UTC)

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(atTime, expectedCheckmarkTime, atTime);
    }

    @Test
    public void testSchedule_laterToday()
    {
        long now = 1422253800000L; // 06:30 jan 26, 2015 (UTC)
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = 1422230400000L; // 00:00 jan 26, 2015 (UTC)
        long expectedReminderTime = 1422261000000L; // 08:30 jan 26, 2015 (UTC)

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));

        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_list()
    {
        long now = 1422277200000L; // 13:00 jan 26, 2015 (UTC)
        DateUtils.setFixedLocalTime(now);

        fixtures.purgeHabits();

        Habit h1 = fixtures.createEmptyHabit();
        h1.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));

        Habit h2 = fixtures.createEmptyHabit();
        h2.setReminder(new Reminder(18, 30, WeekdayList.EVERY_DAY));

        fixtures.createEmptyHabit();

        reminderScheduler.schedule(habitList);

        verify(intentScheduler).schedule(1422347400000L, null);
        verify(intentScheduler).schedule(1422297000000L, null);
        verifyNoMoreInteractions(intentScheduler);
    }

    @Test
    public void testSchedule_tomorrow()
    {
        long now = 1453813200000L; // 13:00 jan 26, 2016 (UTC)
        DateUtils.setFixedLocalTime(now);

        long expectedCheckmarkTime = 1453852800000L; // 00:00 jan 27, 2016 (UTC)
        long expectedReminderTime = 1453883400000L; // 08:30 jan 27, 2016 (UTC)

        habit.setReminder(new Reminder(8, 30, WeekdayList.EVERY_DAY));
        scheduleAndVerify(null, expectedCheckmarkTime, expectedReminderTime);
    }

    @Test
    public void testSchedule_withoutReminder()
    {
        reminderScheduler.schedule(habit, null);
        verifyZeroInteractions(intentScheduler);
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
