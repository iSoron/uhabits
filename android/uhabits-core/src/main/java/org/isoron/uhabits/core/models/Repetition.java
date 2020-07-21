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

package org.isoron.uhabits.core.models;

import org.apache.commons.lang3.builder.*;
import org.isoron.uhabits.core.utils.DateFormats;
import org.isoron.uhabits.core.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;

/**
 * Represents a record that the user has performed or skipped a certain habit at a certain
 * date.
 */
public final class Repetition
{

    private final Timestamp timestamp;

    /**
     * The value of the repetition.
     *
     * For boolean habits, this equals Checkmark.CHECKED_EXPLICITLY if performed
     * or Checkmark.SKIPPED_EXPLICITLY if skipped.
     * For numerical habits, this number is stored in thousandths. That
     * is, if the user enters value 1.50 on the app, it is here stored as 1500.
     */
    private final int value;

    /**
     * Creates a new repetition with given parameters.
     * <p>
     * The timestamp corresponds to the days this repetition occurred. Time of
     * day must be midnight (UTC).
     *
     * @param timestamp the time this repetition occurred.
     */
    public Repetition(Timestamp timestamp, int value)
    {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repetition that = (Repetition) o;

        return new EqualsBuilder()
                .append(timestamp, that.timestamp)
                .append(value, that.value)
                .isEquals();
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(timestamp)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
                .append("timestamp", timestamp)
                .append("value", value)
                .toString();
    }
}
