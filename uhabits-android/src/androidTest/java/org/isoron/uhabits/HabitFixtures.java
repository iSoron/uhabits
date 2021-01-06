/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.DateUtils;

import static org.isoron.uhabits.core.models.Entry.*;

public class HabitFixtures
{
    public boolean LONG_HABIT_ENTRIES[] = {
        true, false, false, true, true, true, false, false, true, true
    };

    public int LONG_NUMERICAL_HABIT_ENTRIES[] = {
        200000, 0, 150000, 137000, 0, 0, 500000, 30000, 100000, 0, 300000,
        100000, 0, 100000
    };

    private ModelFactory modelFactory;

    private final HabitList habitList;

    public HabitFixtures(ModelFactory modelFactory, HabitList habitList)
    {
        this.modelFactory = modelFactory;
        this.habitList = habitList;
    }

    public Habit createEmptyHabit()
    {
        return createEmptyHabit(null);
    }

    public Habit createEmptyHabit(Long id)
    {
        Habit habit = modelFactory.buildHabit();
        habit.setName("Meditate");
        habit.setQuestion("Did you meditate this morning?");
        habit.setDescription("This is a test description");
        habit.setColor(new PaletteColor(5));
        habit.setFrequency(Frequency.DAILY);
        habit.setId(id);
        habitList.add(habit);
        return habit;
    }

    public Habit createLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.setFrequency(new Frequency(3, 7));
        habit.setColor(new PaletteColor(7));

        Timestamp today = DateUtils.getToday();
        int marks[] = { 0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27,
            28, 50, 51, 52, 53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80,
            81, 83, 89, 90, 91, 95, 102, 103, 108, 109, 120};

        for (int mark : marks)
            habit.getOriginalEntries().add(new Entry(today.minus(mark), YES_MANUAL));

        habit.recompute();
        return habit;
    }

    public Habit createVeryLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.setFrequency(new Frequency(1, 2));
        habit.setColor(new PaletteColor(11));

        Timestamp today = DateUtils.getToday();
        int marks[] = {0, 3, 5, 6, 7, 10, 13, 14, 15, 18, 21, 22, 23, 24, 27, 28, 30, 31, 34, 37,
                       39, 42, 43, 46, 47, 48, 51, 52, 54, 55, 57, 59, 62, 65, 68, 71, 73, 76, 79,
                       80, 81, 83, 85, 86, 89, 90, 91, 94, 96, 98, 100, 103, 104, 106, 109, 111,
                       112, 113, 115, 117, 120, 123, 126, 129, 132, 134, 136, 139, 141, 142, 145,
                       148, 149, 151, 152, 154, 156, 157, 159, 161, 162, 163, 164, 166, 168, 170,
                       172, 173, 174, 175, 176, 178, 180, 181, 184, 185, 188, 189, 190, 191, 194,
                       195, 197, 198, 199, 200, 202, 205, 208, 211, 213, 215, 216, 218, 220, 222,
                       223, 225, 227, 228, 230, 231, 232, 234, 235, 238, 241, 242, 244, 247, 250,
                       251, 253, 254, 257, 260, 261, 263, 264, 266, 269, 272, 273, 276, 279, 281,
                       284, 285, 288, 291, 292, 294, 296, 297, 299, 300, 301, 303, 306, 307, 308,
                       309, 310, 313, 316, 319, 322, 324, 326, 329, 330, 332, 334, 335, 337, 338,
                       341, 344, 345, 346, 347, 350, 352, 355, 358, 360, 361, 362, 363, 365, 368,
                       371, 373, 374, 376, 379, 380, 382, 384, 385, 387, 389, 390, 392, 393, 395,
                       396, 399, 401, 404, 407, 410, 411, 413, 414, 416, 417, 419, 420, 423, 424,
                       427, 429, 431, 433, 436, 439, 440, 442, 445, 447, 450, 453, 454, 456, 459,
                       460, 461, 464, 466, 468, 470, 473, 474, 475, 477, 479, 481, 482, 483, 486,
                       489, 491, 493, 495, 497, 498, 500, 503, 504, 507, 510, 511, 512, 515, 518,
                       519, 521, 522, 525, 528, 531, 532, 534, 537, 539, 541, 543, 544, 547, 550,
                       551, 554, 556, 557, 560, 561, 564, 567, 568, 569, 570, 572, 575, 576, 579,
                       582, 583, 584, 586, 589};

        for (int mark : marks)
            habit.getOriginalEntries().add(new Entry(today.minus(mark), YES_MANUAL));

        habit.recompute();
        return habit;
    }

    public Habit createLongNumericalHabit()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setName("Read");
        habit.setQuestion("How many pages did you walk today?");
        habit.setType(Habit.NUMBER_HABIT);
        habit.setTargetType(Habit.AT_LEAST);
        habit.setTargetValue(200.0);
        habit.setUnit("pages");
        habitList.add(habit);

        Timestamp timestamp = DateUtils.getToday();
        for (int value : LONG_NUMERICAL_HABIT_ENTRIES)
        {
            habit.getOriginalEntries().add(new Entry(timestamp, value));
            timestamp = timestamp.minus(1);
        }

        habit.recompute();
        return habit;
    }

    public Habit createShortHabit()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setName("Wake up early");
        habit.setQuestion("Did you wake up before 6am?");
        habit.setFrequency(new Frequency(2, 3));
        habitList.add(habit);

        Timestamp timestamp = DateUtils.getToday();
        for (boolean c : LONG_HABIT_ENTRIES)
        {
            if (c) habit.getOriginalEntries().add(new Entry(timestamp, YES_MANUAL));
            timestamp = timestamp.minus(1);
        }

        habit.recompute();
        return habit;
    }

    public synchronized void purgeHabits(HabitList habitList)
    {
        habitList.removeAll();
    }
}
