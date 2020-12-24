/*
 * Copyright (C) 2015-2017 Álinson Santos Xavier <isoron@gmail.com>
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
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import static java.util.Calendar.*;

public final class Timestamp implements Comparable<Timestamp>
{
    public static final long DAY_LENGTH = 86400000;

    public static final Timestamp ZERO = new Timestamp(0);

    private final long unixTime;

    public Timestamp(long unixTime)
    {
        if (unixTime < 0)
            throw new IllegalArgumentException(
                "Invalid unix time: " + unixTime);

        if (unixTime % DAY_LENGTH != 0)
            unixTime = (unixTime / DAY_LENGTH) * DAY_LENGTH;

        this.unixTime = unixTime;
    }

    public Timestamp(GregorianCalendar cal)
    {
        this(cal.getTimeInMillis());
    }

    public static Timestamp from(int year, int javaMonth, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, javaMonth, day, 0, 0, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    public long getUnixTime()
    {
        return unixTime;
    }

    /**
     * Returns -1 if this timestamp is older than the given timestamp, 1 if this
     * timestamp is newer, or zero if they are equal.
     */
    @Override
    public int compareTo(Timestamp other)
    {
        return Long.signum(this.unixTime - other.unixTime);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Timestamp timestamp = (Timestamp) o;

        return new EqualsBuilder()
            .append(unixTime, timestamp.unixTime)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(unixTime).toHashCode();
    }

    /**
     * Given two timestamps, returns whichever timestamp is the oldest one.
     */
    public static Timestamp oldest(Timestamp first, Timestamp second)
    {
        return first.unixTime < second.unixTime ? first : second;
    }

    public Timestamp minus(int days)
    {
        return plus(-days);
    }

    public Timestamp plus(int days)
    {
        return new Timestamp(unixTime + DAY_LENGTH * days);
    }

    /**
     * Returns the number of days between this timestamp and the given one. If
     * the other timestamp equals this one, returns zero. If the other timestamp
     * is older than this one, returns a negative number.
     */
    public int daysUntil(Timestamp other)
    {
        return (int) ((other.unixTime - this.unixTime) / DAY_LENGTH);
    }

    public boolean isNewerThan(Timestamp other)
    {
        return compareTo(other) > 0;
    }

    public boolean isOlderThan(Timestamp other)
    {
        return compareTo(other) < 0;
    }


    public Date toJavaDate()
    {
        return new Date(unixTime);
    }

    public GregorianCalendar toCalendar()
    {
        GregorianCalendar day =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(unixTime);
        return day;
    }

    @Override
    public String toString()
    {
        return DateFormats.getCSVDateFormat().format(new Date(unixTime));
    }

    /**
     * Returns an integer corresponding to the day of the week. Saturday maps
     * to 0, Sunday maps to 1, and so on.
     */
    public int getWeekday()
    {
        return toCalendar().get(DAY_OF_WEEK) % 7;
    }

    Timestamp truncate(DateUtils.TruncateField field, int firstWeekday)
    {
        return new Timestamp(DateUtils.truncate(field, unixTime, firstWeekday));
    }
}
