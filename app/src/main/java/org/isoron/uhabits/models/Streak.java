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
import org.isoron.uhabits.utils.*;

public final class Streak
{
    private final long start;

    private final long end;

    public Streak(long start, long end)
    {
        this.start = start;
        this.end = end;
    }

    public int compareLonger(Streak other)
    {
        if (this.getLength() != other.getLength())
            return Long.signum(this.getLength() - other.getLength());

        return Long.signum(this.getEnd() - other.getEnd());
    }

    public int compareNewer(Streak other)
    {
        return Long.signum(this.getEnd() - other.getEnd());
    }

    public long getEnd()
    {
        return end;
    }

    public long getLength()
    {
        return (end - start) / DateUtils.millisecondsInOneDay + 1;
    }

    public long getStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("start", start)
            .append("end", end)
            .toString();
    }
}
