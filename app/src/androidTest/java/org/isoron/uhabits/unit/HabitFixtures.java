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

package org.isoron.uhabits.unit;

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

public class HabitFixtures
{
    public static final long FIXED_LOCAL_TIME = 1422172800000L; // 8:00am, January 25th, 2015 (UTC)
    public static boolean NON_DAILY_HABIT_CHECKS[] = { true, false, false, true, true, true, false,
            false, true, true };

    public static Habit createNonDailyHabit()
    {
        Habit habit = new Habit();
        habit.name = "Wake up early";
        habit.description = "Did you wake up before 6am?";
        habit.freqNum = 2;
        habit.freqDen = 3;
        habit.save();

        long timestamp = DateHelper.getStartOfToday();
        for(boolean c : NON_DAILY_HABIT_CHECKS)
        {
            if(c) habit.repetitions.toggle(timestamp);
            timestamp -= DateHelper.millisecondsInOneDay;
        }

        return habit;
    }

    public static Habit createEmptyHabit()
    {
        Habit habit = new Habit();
        habit.name = "Meditate";
        habit.description = "Did you meditate this morning?";
        habit.color = ColorHelper.palette[3];
        habit.freqNum = 1;
        habit.freqDen = 1;
        habit.save();
        return habit;
    }

    public static Habit createLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.freqNum = 3;
        habit.freqDen = 7;
        habit.color = ColorHelper.palette[4];
        habit.save();

        long day = DateHelper.millisecondsInOneDay;
        long today = DateHelper.getStartOfToday();
        int marks[] = { 0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27, 28, 50, 51, 52,
                53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80, 81, 83, 89, 90, 91, 95,
                102, 103, 108, 109, 120};

        for(int mark : marks)
            habit.repetitions.toggle(today - mark * day);

        return habit;
    }

    public static void purgeHabits()
    {
        for(Habit h : Habit.getAll(true))
            h.cascadeDelete();
    }

    public static void fixTime()
    {
        DateHelper.setFixedLocalTime(FIXED_LOCAL_TIME);
    }

    public static void releaseTime()
    {
        DateHelper.setFixedLocalTime(null);
    }
}
