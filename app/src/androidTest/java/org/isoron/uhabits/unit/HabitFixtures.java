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

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.HabitList;
import org.isoron.uhabits.utils.DateUtils;

public class HabitFixtures
{
    public boolean NON_DAILY_HABIT_CHECKS[] = {
        true, false, false, true, true, true, false, false, true, true
    };

    private final HabitList habitList;

    public HabitFixtures(HabitList habitList)
    {
        this.habitList = habitList;
    }

    public Habit createEmptyHabit()
    {
        Habit habit = new Habit();
        habit.setName("Meditate");
        habit.setDescription("Did you meditate this morning?");
        habit.setColor(3);
        habit.setFreqNum(1);
        habit.setFreqDen(1);
        habitList.add(habit);
        return habit;
    }

    public Habit createLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.setFreqNum(3);
        habit.setFreqDen(7);
        habit.setColor(4);

        long day = DateUtils.millisecondsInOneDay;
        long today = DateUtils.getStartOfToday();
        int marks[] = { 0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27,
            28, 50, 51, 52, 53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80,
            81, 83, 89, 90, 91, 95, 102, 103, 108, 109, 120};

        for (int mark : marks)
            habit.getRepetitions().toggleTimestamp(today - mark * day);

        return habit;
    }

    public Habit createShortHabit()
    {
        Habit habit = new Habit();
        habit.setName("Wake up early");
        habit.setDescription("Did you wake up before 6am?");
        habit.setFreqNum(2);
        habit.setFreqDen(3);
        habitList.add(habit);

        long timestamp = DateUtils.getStartOfToday();
        for (boolean c : NON_DAILY_HABIT_CHECKS)
        {
            if (c) habit.getRepetitions().toggleTimestamp(timestamp);
            timestamp -= DateUtils.millisecondsInOneDay;
        }

        return habit;
    }

    public void purgeHabits(HabitList habitList)
    {
        for (Habit h : habitList.getAll(true))
            habitList.remove(h);
    }
}
