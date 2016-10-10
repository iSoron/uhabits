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

import android.database.*;

import com.activeandroid.*;
import com.activeandroid.annotation.*;

import org.isoron.uhabits.models.*;

/**
 * The SQLite database record corresponding to a {@link Checkmark}.
 */
@Table(name = "Checkmarks")
public class CheckmarkRecord extends Model implements SQLiteRecord
{
    /**
     * The habit to which this checkmark belongs.
     */
    @Column(name = "habit")
    public HabitRecord habit;

    /**
     * Timestamp of the day to which this checkmark corresponds. Time of the day
     * must be midnight (UTC).
     */
    @Column(name = "timestamp")
    public Long timestamp;

    /**
     * Indicates whether there is a repetition at the given timestamp or not,
     * and whether the repetition was expected. Assumes one of the values
     * UNCHECKED, CHECKED_EXPLICITLY or CHECKED_IMPLICITLY.
     */
    @Column(name = "value")
    public Integer value;

    @Override
    public void copyFrom(Cursor c)
    {
        timestamp = c.getLong(1);
        value = c.getInt(2);
    }

    public Checkmark toCheckmark()
    {
        return new Checkmark(timestamp, value);
    }
}
