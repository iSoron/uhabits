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

package org.isoron.uhabits.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Checkmarks")
public class Checkmark extends Model
{
    /**
     * Indicates that there was no repetition at the timestamp, even though a repetition was
     * expected.
     */
    public static final int UNCHECKED = 0;

    /**
     * Indicates that there was no repetition at the timestamp, but one was not expected in any
     * case, due to the frequency of the habit.
     */
    public static final int CHECKED_IMPLICITLY = 1;

    /**
     * Indicates that there was a repetition at the timestamp.
     */
    public static final int CHECKED_EXPLICITLY = 2;

    /**
     * The habit to which this checkmark belongs.
     */
    @Column(name = "habit")
    public Habit habit;

    /**
     * Timestamp of the day to which this checkmark corresponds. Time of the day must be midnight
     * (UTC).
     */
    @Column(name = "timestamp")
    public Long timestamp;

    /**
     * Indicates whether there is a repetition at the given timestamp or not, and whether the
     * repetition was expected. Assumes one of the values UNCHECKED, CHECKED_EXPLICITLY or
     * CHECKED_IMPLICITLY.
     */
    @Column(name = "value")
    public Integer value;
}
