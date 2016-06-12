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
 * The SQLite database record corresponding to a {@link Repetition}.
 */
@Table(name = "Repetitions")
public class RepetitionRecord extends Model
{
    @Column(name = "habit")
    public HabitRecord habit;

    @Column(name = "timestamp")
    public Long timestamp;

    public void copyFrom(Repetition repetition)
    {
        habit = HabitRecord.get(repetition.getHabit().getId());
        timestamp = repetition.getTimestamp();
    }

    public static RepetitionRecord get(Long id)
    {
        return RepetitionRecord.load(RepetitionRecord.class, id);
    }

    public Repetition toRepetition()
    {
        SQLiteHabitList habitList = SQLiteHabitList.getInstance();
        Habit h = habitList.getById(habit.getId());
        return new Repetition(h, timestamp);
    }
}
