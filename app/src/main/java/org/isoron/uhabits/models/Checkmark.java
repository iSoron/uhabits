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

import org.apache.commons.lang3.builder.*;

/**
 * A Checkmark represents the completion status of the habit for a given day.
 * <p>
 * While repetitions simply record that the habit was performed at a given date,
 * a checkmark provides more information, such as whether a repetition was
 * expected at that day or not.
 * <p>
 * Checkmarks are computed automatically from the list of repetitions.
 */
public final class Checkmark
{
    /**
     * Indicates that there was a repetition at the timestamp.
     */
    public static final int CHECKED_EXPLICITLY = 2;

    /**
     * Indicates that there was no repetition at the timestamp, but one was not
     * expected in any case, due to the frequency of the habit.
     */
    public static final int CHECKED_IMPLICITLY = 1;

    /**
     * Indicates that there was no repetition at the timestamp, even though a
     * repetition was expected.
     */
    public static final int UNCHECKED = 0;

    private final long timestamp;

    private final int value;

    public Checkmark(long timestamp, int value)
    {
        this.timestamp = timestamp;
        this.value = value;
    }

    public int compareNewer(Checkmark other)
    {
        return Long.signum(this.getTimestamp() - other.getTimestamp());
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("timestamp", timestamp)
            .append("value", value)
            .toString();
    }
}
