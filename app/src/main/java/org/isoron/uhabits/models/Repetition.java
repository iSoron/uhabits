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
 * Represents a record that the user has performed a certain habit at a certain
 * date.
 */
public final class Repetition
{

    private final long timestamp;

    /**
     * Creates a new repetition with given parameters.
     * <p>
     * The timestamp corresponds to the days this repetition occurred. Time of
     * day must be midnight (UTC).
     *
     * @param timestamp the time this repetition occurred.
     */
    public Repetition(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("timestamp", timestamp)
            .toString();
    }
}
