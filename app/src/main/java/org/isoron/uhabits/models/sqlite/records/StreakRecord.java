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

package org.isoron.uhabits.models.sqlite.records;

import com.activeandroid.*;
import com.activeandroid.annotation.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.*;

/**
 * The SQLite database record corresponding to a Streak.
 */
@Table(name = "Streak")
public class StreakRecord extends Model
{
    @Column(name = "habit")
    public HabitRecord habit;

    @Column(name = "start")
    public Long start;

    @Column(name = "end")
    public Long end;

    @Column(name = "length")
    public Long length;

    public static StreakRecord get(Long id)
    {
        return StreakRecord.load(StreakRecord.class, id);
    }

    public void copyFrom(Streak streak)
    {
        habit = HabitRecord.get(streak.getHabit().getId());
        start = streak.getStart();
        end = streak.getEnd();
        length = streak.getLength();
    }

    public Streak toStreak()
    {
        SQLiteHabitList habitList = SQLiteHabitList.getInstance();
        Habit h = habitList.getById(habit.getId());
        return new Streak(h, start, end);
    }
}
