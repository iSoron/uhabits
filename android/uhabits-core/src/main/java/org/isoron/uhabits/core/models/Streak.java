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

import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;

public final class Streak
{
    private final Timestamp start;

    private final Timestamp end;

    public Streak(Timestamp start, Timestamp end)
    {
        this.start = start;
        this.end = end;
    }

    public int compareLonger(Streak other)
    {
        if (this.getLength() != other.getLength())
            return Long.signum(this.getLength() - other.getLength());

        return compareNewer(other);
    }

    public int compareNewer(Streak other)
    {
        return end.compareTo(other.end);
    }

    public Timestamp getEnd()
    {
        return end;
    }

    public int getLength()
    {
        return start.daysUntil(end) + 1;
    }

    public Timestamp getStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
            .append("start", start)
            .append("end", end)
            .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Streak streak = (Streak) o;

        return new EqualsBuilder()
            .append(start, streak.start)
            .append(end, streak.end)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(start)
            .append(end)
            .toHashCode();
    }
}
