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

import androidx.annotation.NonNull;
import org.apache.commons.lang3.builder.*;

import javax.annotation.concurrent.*;

import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;

/**
 * A Checkmark represents the completion status of the habit for a given day.
 * <p>
 * While repetitions simply record that the habit was performed at a given date,
 * a checkmark provides more information, such as whether a repetition was
 * expected at that day or not.
 * <p>
 * Checkmarks are computed automatically from the list of repetitions.
 */
@ThreadSafe
public final class Checkmark
{
    /**
     * Indicates that there was an explicit skip at the timestamp.
     */
    public static final int SKIP = 2;

    /**
     * Indicates that there was a repetition at the timestamp.
     */
    public static final int YES = 1;

    /**
     * Indicates that there was no repetition at the timestamp, even though a
     * repetition was expected.
     */
    public static final int NO = 0;

    private final Timestamp timestamp;

    /**
     * The state of the checkmark.
     */
    @NonNull
    private final CheckmarkState state;

    public Checkmark(Timestamp timestamp, int value, boolean manualInput)
    {
        this.timestamp = timestamp;
        this.state = new CheckmarkState(value, manualInput);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Checkmark checkmark = (Checkmark) o;

        return new EqualsBuilder()
            .append(timestamp, checkmark.timestamp)
            .append(getState(), checkmark.getState())
            .isEquals();
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public CheckmarkState getState()
    {
        return state;
    }

    public int getValue()
    {
        return state.getValue();
    }

    public boolean isManualInput()
    {
        return state.isManualInput();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(timestamp)
            .append(getState())
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
            .append("timestamp", timestamp)
            .append("value", getValue())
            .append("manualInput", isManualInput())
            .toString();
    }
}
