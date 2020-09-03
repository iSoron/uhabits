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

import androidx.annotation.*;

import org.apache.commons.lang3.builder.*;
import org.isoron.uhabits.core.utils.*;

import static org.isoron.uhabits.core.utils.StringUtils.*;

public final class Reminder
{
    private final int hour;

    private final int minute;

    public Reminder(int hour, int minute)
    {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public long getTimeInMillis()
    {
        return DateUtils.getUpcomingTimeInMillis(hour, minute);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Reminder reminder = (Reminder) o;

        return new EqualsBuilder()
                .append(hour, reminder.hour)
                .append(minute, reminder.minute)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(hour)
                .append(minute)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
                .append("hour", hour)
                .append("minute", minute)
                .toString();
    }
}
