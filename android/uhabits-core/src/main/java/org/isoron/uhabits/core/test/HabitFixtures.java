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

package org.isoron.uhabits.core.test;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.*;
import org.isoron.uhabits.core.utils.*;

public class HabitFixtures
{
    public boolean NON_DAILY_HABIT_CHECKS[] = {
            true, false, false, true, true, true, false, false, true, true
    };

    private final ModelFactory modelFactory;

    private HabitList habitList;

    public HabitFixtures(ModelFactory modelFactory, HabitList habitList)
    {
        this.modelFactory = modelFactory;
        this.habitList = habitList;
    }

    public Habit createEmptyHabit()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setName("Meditate");
        habit.setQuestion("Did you meditate this morning?");
        habit.setColor(3);
        habit.setFrequency(Frequency.DAILY);
        saveIfSQLite(habit);

        return habit;
    }

    public Habit createLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.setFrequency(new Frequency(3, 7));
        habit.setColor(4);

        Timestamp today = DateUtils.getToday();
        int marks[] = {0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27,
                28, 50, 51, 52, 53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80,
                81, 83, 89, 90, 91, 95, 102, 103, 108, 109, 120};

        for (int mark : marks)
            habit.getRepetitions().toggle(today.minus(mark));

        return habit;
    }

    public Habit createNumericalHabit()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setType(Habit.NUMBER_HABIT);
        habit.setName("Run");
        habit.setQuestion("How many miles did you run today?");
        habit.setUnit("miles");
        habit.setTargetType(Habit.AT_LEAST);
        habit.setTargetValue(2.0);
        habit.setColor(1);
        saveIfSQLite(habit);

        Timestamp today = DateUtils.getToday();
        int times[] = {0, 1, 3, 5, 7, 8, 9, 10};
        int values[] = {100, 200, 300, 400, 500, 600, 700, 800};

        for (int i = 0; i < times.length; i++)
        {
            Timestamp timestamp = today.minus(times[i]);
            habit.getRepetitions().add(new Repetition(timestamp, values[i]));
        }

        return habit;
    }

    public Habit createLongNumericalHabit(Timestamp reference)
    {
        Habit habit = modelFactory.buildHabit();
        habit.setType(Habit.NUMBER_HABIT);
        habit.setName("Walk");
        habit.setQuestion("How many steps did you walk today?");
        habit.setUnit("steps");
        habit.setTargetType(Habit.AT_LEAST);
        habit.setTargetValue(100);
        habit.setColor(1);
        saveIfSQLite(habit);

        int times[] = {0, 5, 9, 15, 17, 21, 23, 27, 28, 35, 41, 45, 47, 53, 56, 62, 70, 73, 78,
                83, 86, 94, 101, 106, 113, 114, 120, 126, 130, 133, 141, 143, 148, 151, 157, 164,
                166, 171, 173, 176, 179, 183, 191, 259, 264, 268, 270, 275, 282, 284, 289, 295,
                302, 306, 310, 315, 323, 325, 328, 335, 343, 349, 351, 353, 357, 359, 360, 367,
                372, 376, 380, 385, 393, 400, 404, 412, 415, 418, 422, 425, 433, 437, 444, 449,
                455, 460, 462, 465, 470, 471, 479, 481, 485, 489, 494, 495, 500, 501, 503, 507};

        int values[] = {230, 306, 148, 281, 134, 285, 104, 158, 325, 236, 303, 210, 118, 124,
                301, 201, 156, 376, 347, 367, 396, 134, 160, 381, 155, 354, 231, 134, 164, 354,
                236, 398, 199, 221, 208, 397, 253, 276, 214, 341, 299, 221, 353, 250, 341, 168,
                374, 205, 182, 217, 297, 321, 104, 237, 294, 110, 136, 229, 102, 271, 250, 294,
                158, 319, 379, 126, 282, 155, 288, 159, 215, 247, 207, 226, 244, 158, 371, 219,
                272, 228, 350, 153, 356, 279, 394, 202, 213, 214, 112, 248, 139, 245, 165, 256,
                370, 187, 208, 231, 341, 312};

        for (int i = 0; i < times.length; i++)
        {
            Timestamp timestamp = reference.minus(times[i]);
            habit.getRepetitions().add(new Repetition(timestamp, values[i]));
        }

        return habit;
    }

    public Habit createShortHabit()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setName("Wake up early");
        habit.setQuestion("Did you wake up before 6am?");
        habit.setFrequency(new Frequency(2, 3));
        saveIfSQLite(habit);

        Timestamp timestamp = DateUtils.getToday();
        for (boolean c : NON_DAILY_HABIT_CHECKS)
        {
            if (c) habit.getRepetitions().toggle(timestamp);
            timestamp = timestamp.minus(1);
        }

        return habit;
    }

    private void saveIfSQLite(Habit habit)
    {
        if (!(habit.getRepetitions() instanceof SQLiteRepetitionList)) return;
        habitList.add(habit);
    }
}
